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
import com.kyas.wolkandhold.data.api.GameSocketManager;
import com.kyas.wolkandhold.data.api.requests.PolygonRequest;
import com.kyas.wolkandhold.data.api.requests.UserRequest;
import com.kyas.wolkandhold.data.api.response.PolygonResponse;
import com.kyas.wolkandhold.data.database.AppDatabase;
import com.kyas.wolkandhold.data.database.RouteRepository;
import com.kyas.wolkandhold.data.database.dao.PolygonDao;
import com.kyas.wolkandhold.data.database.dao.RouteDao;
import com.kyas.wolkandhold.data.database.dao.RoutePointDao;
import com.kyas.wolkandhold.data.models.PlayerModel;
import com.kyas.wolkandhold.data.database.entities.Polygon;
import com.kyas.wolkandhold.data.database.entities.Route;
import com.yandex.mapkit.geometry.Point;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataRepository {


    private static volatile DataRepository INSTANCE;
    private final MutableLiveData<List<Point>> points = new MutableLiveData<>(new ArrayList<>());
    private final LiveData<List<Polygon>> polygons;
    private final MutableLiveData<Map<Long, PlayerModel>> players;
    private final MutableLiveData<Boolean> polygonSaved = new MutableLiveData<>();
    private final MutableLiveData<List<Route>> routes = new MutableLiveData<>();
    private final MutableLiveData<Point> location = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final FusedLocationProviderClient mFusedLocationClient;
    private final SharedPreferences set;
    private final AppDatabase db;

    private Retrofit retrofit;
    private final ApiService apiService;
    private final GameSocketManager socketManager;


    public DataRepository(Context context) {
        db = AppDatabase.getInstance(context);
        executor.execute(() -> {
            routes.postValue(db.getRouteDao().getAllRoutes());
        });
        set = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        polygons = db.getPolygonDao().getAllPolygonsLive();
        players = new MutableLiveData<>();
        players.setValue(new HashMap<>());
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(set.getString("jwt", "token")))
                .build();
        this.socketManager = new GameSocketManager();

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        setPolygonListener();
        setPlayerListener();
    }

    public static DataRepository getInstance(Context context) {
        if (INSTANCE == null) {
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
            saveRouteInternal(routeName, points);
        });
    }

    public void saveRoute(String routeName, List<Point> points) {
        executor.execute(() -> {
            saveRouteInternal(routeName, points);
        });
    }

    private void saveRouteInternal(String routeName, List<Point> points) {
        RouteDao rd = db.getRouteDao();
        RoutePointDao rpd = db.getRoutePointDao();
        PolygonDao pd = db.getPolygonDao();
        // Сохраняем маршрут и его точки в базу данных
        RouteRepository routeRep = new RouteRepository(rd, rpd);
        // Используем LOCAL_USER_ID для обозначения собственных маршрутов пользователя
        routeRep.addNewRoute(routeName, getDistance(points), Constants.LOCAL_USER_ID);
        routeRep.addPointsToRoute(points);
        
        // Обновляем или создаем новую территорию для локального пользователя
        // LOCAL_USER_ID означает, что это собственный полигон пользователя
        List<Polygon> polys = pd.getPolygonsByUser(Constants.LOCAL_USER_ID);
        Polygon dbPoly;
        if (polys.isEmpty()) {
            dbPoly = new Polygon();
            dbPoly.userId = Constants.LOCAL_USER_ID;
            dbPoly.ownerName = "Мой полигон";
        } else {
            dbPoly = polys.get(0).copyPolygon();
        }
        dbPoly.userId = Constants.LOCAL_USER_ID;
        dbPoly.lastUpdated = System.currentTimeMillis();
        Gson gson = new Gson();
        dbPoly.pointsJson = gson.toJson(points);
        dbPoly.area = polygonAreaOnEarth(points);
        // Используем upsert для автоматического обновления или создания
        pd.upsert(dbPoly);
        sendUpsertPolygonRequest(dbPoly);

        Log.d("DialogSaveRoute", "saved new route with id:" + routeRep.currentRouteId);
        reloadRoutes();
    }

    public void renameRoute(Route route, String newName) {
        executor.execute(() -> {
            route.name = newName;
            RouteRepository rr = new RouteRepository(db.getRouteDao(), db.getRoutePointDao());
            rr.updateRoute(route);
            reloadRoutes();
        });
    }

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

    public LiveData<List<Polygon>> getPolygons() {
        return polygons;
    }

    public LiveData<Boolean> getPolygonSaved() {
        return polygonSaved;
    }

    public MutableLiveData<Map<Long, PlayerModel>> getPlayers() {
        return players;
    }

    public void loadPolygons() {
        apiService.getPolygonsInRadius(
                location.getValue().getLatitude(),
                location.getValue().getLongitude(), Constants.DEFAULT_SEARCH_RADIUS_METERS).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<PolygonResponse>> call, @NonNull Response<List<PolygonResponse>> response) {
                if (response.isSuccessful()) {
                    List<PolygonResponse> responses = response.body();
                    if (responses != null) {
                        executor.execute(() -> {
                            List<Polygon> list = new ArrayList<>();
                            responses.forEach((r) -> {
                                list.addAll(r.toEntities());
                            });
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
    public void startGameSession(String token) {
        socketManager.connect(token);
    }

    // Метод для отправки координат из GPS-сервиса
    public void emitLocation(double lat, double lon, boolean isCapture) {
        socketManager.sendLocation(lat, lon, isCapture);
    }

    // Подписка на данные (можно через LiveData или колбэки)
    public void setPolygonListener() {
        socketManager.setPolygonListener(polys -> {
            executor.execute(() -> {
                db.getPolygonDao().insertAll(polys);
            });
        });
    }

    public void setPlayerListener() {
        socketManager.setPlayerListener(player -> {
            Map<Long, PlayerModel> currentMap = players.getValue();
            if (currentMap == null) currentMap = new HashMap<>();
            Map<Long, PlayerModel> updatedMap = new HashMap<>(currentMap);
            updatedMap.put(player.playerId, player);
            players.postValue(updatedMap);
        });
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
