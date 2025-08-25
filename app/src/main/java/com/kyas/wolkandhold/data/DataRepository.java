package com.kyas.wolkandhold.data;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kyas.wolkandhold.BuildConfig;
import com.kyas.wolkandhold.data.api.ApiService;
import com.kyas.wolkandhold.data.api.AuthInterceptor;
import com.kyas.wolkandhold.data.api.requests.PolygonRequest;
import com.kyas.wolkandhold.data.api.requests.UserRequest;
import com.kyas.wolkandhold.data.api.response.PolygonResponse;
import com.kyas.wolkandhold.data.database.AppDatabase;
import com.kyas.wolkandhold.data.database.RouteRepository;
import com.kyas.wolkandhold.data.database.dao.PolygonDao;
import com.kyas.wolkandhold.data.database.dao.RouteDao;
import com.kyas.wolkandhold.data.database.dao.RoutePointDao;
import com.kyas.wolkandhold.data.database.entities.Polygon;
import com.kyas.wolkandhold.data.database.entities.Route;
import com.yandex.mapkit.geometry.Point;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class DataRepository {
    private static volatile DataRepository INSTANCE;
    private final MutableLiveData<List<Point>> points = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Polygon>> polygons = new MutableLiveData<>();
    private final MutableLiveData<Boolean> polygonSaved = new MutableLiveData<>();
    private final MutableLiveData<List<Route>> routes = new MutableLiveData<>();
    private final MutableLiveData<Point> location = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final FusedLocationProviderClient mFusedLocationClient;
    private final SharedPreferences set;
    private final AppDatabase db;

    private Retrofit retrofit;
    private final ApiService apiService;
    private StompClient stompClient;


    public DataRepository(Context context) {
        db = AppDatabase.getInstance(context);
        executor.execute(() -> {
            routes.postValue(db.getRouteDao().getAllRoutes());
        });
        set = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(set.getString("jwt", "token")))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public static DataRepository getInstance(Context context) {
        if (INSTANCE == null) {
            // CHANGE: Лочимся на правильном классе DataRepository
            synchronized (DataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<Point>> getPoints() {
        return points;
    }
    public double distanceFirstToLast() {
        //Расчет дистации для замыкания круга
        double x1lat = points.getValue().get(0).getLatitude();
        double y1lon = points.getValue().get(0).getLongitude();
        double x2lat = points.getValue().get(points.getValue().size()-1).getLatitude();
        double y2lon = points.getValue().get(points.getValue().size()-1).getLongitude();
        float[] distance = new float[1];
        Location.distanceBetween(x1lat, y1lon, x2lat, y2lon, distance);
        return distance[0];
    }

    public void addPoint(Point point) {
        List<Point> current = new ArrayList<>(points.getValue());
        current.add(point);
        points.postValue(current);
    }
    public void clearPoints() {
        points.postValue(new ArrayList<>());
    }

    public MutableLiveData<List<Route>> getRoutes() {
        return routes;
    }

    // CHANGE: Перезагрузка списка маршрутов из БД
    private void reloadRoutes() {
        executor.execute(() -> routes.postValue(db.getRouteDao().getAllRoutes()));
    }

    public void saveRoute(String routeName) {
        executor.execute(() -> {
            List<Point> points = this.points.getValue();
            RouteDao rd = db.getRouteDao();
            RoutePointDao rpd = db.getRoutePointDao();
            PolygonDao pd = db.getPolygonDao();
            //сохраняем маршрут и его точки в базу данных
            RouteRepository routeRep = new RouteRepository(rd, rpd);
            routeRep.addNewRoute(routeName, getDistance(points), -1);
            routeRep.addPointsToRoute(points);
            // Обновляем или создаем новую территорию для этого юзера
            List<Polygon> polys = pd.getPolygonsByUser(-1);
            Polygon dbPoly = polys.isEmpty() ? new Polygon() : polys.get(0).copyPolygon();
            dbPoly.userId = -1;
            dbPoly.lastUpdated = System.currentTimeMillis();
            Gson gson = new Gson();
            dbPoly.pointsJson = gson.toJson(points);
            dbPoly.area = polygonAreaOnEarth(points);
            if (polys.isEmpty()) {
                pd.addPolygon(dbPoly);
            } else {
                if (dbPoly.area > polys.get(0).area) {
                    pd.updatePolygon(dbPoly);
                }
            }
            sendUpsertPolygonRequest(dbPoly);

            Log.d("DialogSaveRoute", "saved new route with id:" + routeRep.currentRouteId);
            // CHANGE: Обновляем список маршрутов
            reloadRoutes();
        });
    }

    // CHANGE: Переименование маршрута
    public void renameRoute(Route route, String newName) {
        executor.execute(() -> {
            route.name = newName;
            RouteRepository rr = new RouteRepository(db.getRouteDao(), db.getRoutePointDao());
            rr.updateRoute(route);
            reloadRoutes();
        });
    }

    // CHANGE: Удаление маршрута с каскадным удалением точек
    public void deleteRoute(long routeId) {
        executor.execute(() -> {
            RouteRepository rr = new RouteRepository(db.getRouteDao(), db.getRoutePointDao());
            rr.deleteRoute(routeId);
            reloadRoutes();
        });
    }
    public double getDistance(List<Point> points) {
        if (points == null || points.size() < 2) return 0.0;

        double result = 0.0;
        float[] distance = new float[1];

        for (int i = 0; i < points.size() - 1; i++) {
            Location.distanceBetween(
                    points.get(i).getLatitude(), points.get(i).getLongitude(),
                    points.get(i + 1).getLatitude(), points.get(i + 1).getLongitude(),
                    distance
            );
            result += distance[0];
        }

        return result;
    }
    public void sendUpsertPolygonRequest(Polygon poly) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Point>>(){}.getType();
        List<Point> points = gson.fromJson(poly.pointsJson, listType);

        apiService.upsertPolygon(new PolygonRequest(
                new UserRequest(poly.userId, poly.ownerName),
                points,
                poly.area,
                poly.lastUpdated)).enqueue(new Callback<PolygonResponse>() {
            @Override
            public void onResponse(@NonNull Call<PolygonResponse> call, @NonNull Response<PolygonResponse> response) {
                polygonSaved.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(@NonNull Call<PolygonResponse> call, @NonNull Throwable t) {
                polygonSaved.postValue(false);
            }
        });
    }

    public MutableLiveData<List<Polygon>> getPolygons() {
        return polygons;
    }

    public LiveData<Boolean> getPolygonSaved() {
        return polygonSaved;
    }
    public void loadPolygons() {
        apiService.getPolygonsInRadius(
                location.getValue().getLatitude(),
                location.getValue().getLongitude(), 100).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<PolygonResponse>> call, @NonNull Response<List<PolygonResponse>> response) {
                if (response.isSuccessful()) {
                    List<PolygonResponse> responses = response.body();
                    if (responses != null) {
                        List<Polygon> list = new ArrayList<>();
                        responses.forEach((r) -> {
                            list.add(r.toEntity());
                        });
                        polygons.postValue(list);
                        executor.execute(() -> {
                            PolygonDao dao = db.getPolygonDao();
                            list.forEach(dao::upsert);
                        });
                    }
                } else {
                    Log.d("API", "Response get polygons not successful");
                }
            }

            @Override
            public void onFailure(Call<List<PolygonResponse>> call, Throwable t) {

                Log.d("API", "Failure response get polygons " + t.getLocalizedMessage());
            }
        });
    }
    private double polygonAreaOnEarth(List<Point> coords) {
        if (coords.size() < 3) return 0;

        double total = 0.0;
        int n = coords.size();

        for (int i = 0; i < n; i++) {
            double[] p1 = {coords.get(i).getLatitude(), coords.get(i).getLongitude()};
            double[] p2 = {coords.get((i + 1) % n).getLatitude(), coords.get((i + 1) % n).getLongitude()};

            double lat1 = Math.toRadians(p1[0]);
            double lon1 = Math.toRadians(p1[1]);
            double lat2 = Math.toRadians(p2[0]);
            double lon2 = Math.toRadians(p2[1]);

            total += (lon2 - lon1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }

        double earthRadius = 6378137;
        return Math.abs(total * earthRadius * earthRadius / 2.0);
    }
    @SuppressLint("CheckResult")
    public void connectWebSocket() {
        stompClient = Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                BuildConfig.WS_URL
        );

        stompClient.lifecycle()
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d("WS", "Stomp connection opened");

                            stompClient.topic("/user/queue/polygons")
                                    .subscribe(msg -> {
                                        Log.d("WS", "Got: " + msg.getPayload());
                                    }, err -> {
                                        Log.e("WS", "Topic error", err);
                                    });

                            String body = String.format(Locale.getDefault(), "{\"lat\":%f,\"lon\":%f,\"radius\":100}",
                                    location.getValue().getLatitude(),
                                    location.getValue().getLongitude());
                            stompClient.send("/app/subscribe", body)
                                    .subscribe(() -> Log.d("WS", "Send OK"),
                                            err -> Log.e("WS", "Send error", err));
                            break;

                        case ERROR:
                            Log.e("WS", "Error", lifecycleEvent.getException());
                            break;

                        case CLOSED:
                            Log.d("WS", "Connection closed");
                            break;
                    }
                });
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", "Bearer " + set.getString("jwt", "null")));
        Log.d("WS", headers.toString());
        stompClient.connect(headers);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location loc = task.getResult();
                if (loc == null) {
                    requestNewLocationData();
                } else {
                    location.postValue(new Point(loc.getLatitude(), loc.getLongitude()));
                    // CHANGE: Исправлен лог: выводим широту и долготу
                    Log.d("GPS", "getLocation: " + loc.getLatitude() + " | " + loc.getLongitude());

                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(0)
                .setMaxUpdates(1)
                .build();

        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper()
        );
    }
    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            assert mLastLocation != null;
            location.setValue(new Point(
                    mLastLocation.getLatitude(),
                    mLastLocation.getLongitude()
            ));
        }
    };

    public MutableLiveData<Point> getLocation() {
        return location;
    }
}
