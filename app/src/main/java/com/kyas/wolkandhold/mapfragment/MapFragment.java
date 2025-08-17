package com.kyas.wolkandhold.mapfragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kyas.wolkandhold.DialogFactory;
import com.kyas.wolkandhold.R;
import com.kyas.wolkandhold.RouteRepository;
import com.kyas.wolkandhold.RouteViewModel;
import com.kyas.wolkandhold.database.AppDatabase;
import com.kyas.wolkandhold.database.dao.PolygonDao;
import com.kyas.wolkandhold.database.dao.RouteDao;
import com.kyas.wolkandhold.database.dao.RoutePointDao;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.LinearRing;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polygon;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolygonMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment implements UserLocationObjectListener {


    private static final int PERMISSION_ID = 44;
    private MapView mapView;
    private Activity activity;
    FusedLocationProviderClient mFusedLocationClient;
    private ExtendedFloatingActionButton btnStartRecording;
    private FloatingActionButton btnCenterLocation;
    private boolean isRecording = false;
    private ExecutorService executor;
    private RouteViewModel routeViewModel;
    private PolylineMapObject recordingPolyline;
    private PlacemarkMapObject markSartRoute;
    private Map<Long, PolygonData> polygonsMapObjects;
    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BROADCAST_RECEIVER", "onReceive: new Point");
            routeViewModel.updatePoints();
        }
    };


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapview);
        btnStartRecording = view.findViewById(R.id.fab_start_record);
        btnCenterLocation = view.findViewById(R.id.fab_center_location);
        polygonsMapObjects = new HashMap<>();

        UserLocationLayer ull = MapKitFactory.getInstance().createUserLocationLayer(mapView.getMapWindow());
        ull.setObjectListener(this);
        ull.setVisible(true);
        ull.setHeadingModeActive(true);

        routeViewModel = new ViewModelProvider(this).get(RouteViewModel.class);
        routeViewModel.getPoints().observe(getViewLifecycleOwner(), updatedPoints -> {
            if (recordingPolyline != null) {
                recordingPolyline.setGeometry(new Polyline(updatedPoints));
                if (updatedPoints.isEmpty()) {
                    return;
                }
                mapView.getMapWindow().getMap().move(
                        new CameraPosition(updatedPoints.get(updatedPoints.size()-1), 17f, 0, 20),
                        new Animation(Animation.Type.SMOOTH, 1),
                        null
                );
            } else {
                mapView.getMapWindow()
                        .getMap()
                        .getMapObjects()
                        .addPolygon(new Polygon(new LinearRing(updatedPoints), new ArrayList<>()));
            }
        });
        loadPolygons();


        btnStartRecording.setOnClickListener(v -> {
            Intent service = new Intent(activity, LocationRecordService.class);
            if (isRecording) {
                // Остановить запись
                List<Point> points = BufferedRoute.getAll();
                if (points.size() <= 3) {
                    DialogFactory.showConfirmDialog(activity, R.string.dialog_title_save_route, R.string.dialog_message_short_route, () -> {
                        stopRecordingWithoutSave(service);
                    });
                    Log.d("stopRecording", "too low points " + points.size());
                    return;
                }
                //Расчет дистации для замыкания круга
                double x1lat = points.get(0).getLatitude();
                double y1lon = points.get(0).getLongitude();
                double x2lat = points.get(points.size()-1).getLatitude();
                double y2lon = points.get(points.size()-1).getLongitude();
                float[] distance = new float[1];
                Location.distanceBetween(x1lat, y1lon, x2lat, y2lon, distance);

                if (distance[0] > 100){
                    DialogFactory.showConfirmDialog(activity, R.string.dialog_title_save_route, R.string.dialog_message_big_difference_route, () -> {
                        stopRecordingWithoutSave(service);
                    });
                    Log.d("stopRecording", "very much distance length " + distance[0] + ", not saved!");
                    return;
                }

                //если маршрут может быть соединен в круг останавливаем запись

                DialogFactory.showSaveRouteDialog(activity,name -> {
                    stopRecordingWithSave(service, name);
                });
            } else {
                startRecording(service);
            }
        });
        btnStartRecording.setOnLongClickListener((v) -> {
            if (btnStartRecording.isExtended()) {
                btnStartRecording.shrink();
            } else {
                btnStartRecording.extend();
            }
            return true;
        });
        btnCenterLocation.setOnClickListener((v) -> {
            getLastLocation();
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = requireActivity();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        getLastLocation();
        executor = Executors.newSingleThreadExecutor();

    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        routeViewModel.updatePoints();
        Log.d("Fragment", "Resume map fragment");
    }


    private void startRecording(Intent service) {
        // Начать запись
        btnStartRecording.setText(R.string.stop_record);
        btnStartRecording.setIconResource(R.drawable.ic_stop);
        markSartRoute = mapView.getMapWindow().getMap().getMapObjects().addPlacemark();
        getLastLocation();
        //когда координаты пользователя найдены запускаем сервис
        routeViewModel.getLocation().observe(getViewLifecycleOwner(), (location) -> {
            if (markSartRoute.isValid()) {
                markSartRoute.setGeometry(location);
                markSartRoute.setIcon(ImageProvider.fromResource(activity, R.drawable.ic_pin));
                if (checkPermissions()) {
                    recordingPolyline = mapView.getMapWindow().getMap().getMapObjects().addPolyline();

                    ContextCompat.startForegroundService(activity, service);
                    IntentFilter filter = new IntentFilter("LOCATION_UPDATE");
                    LocalBroadcastManager.getInstance(activity).registerReceiver(locationReceiver, filter);
                    isRecording = true;
                }
            }
        });
    }
    private void stopRecordingWithSave(Intent service, String routeName) {
        // Остановить запись
        btnStartRecording.setIconResource(R.drawable.ic_play);
        btnStartRecording.setText(R.string.start_record);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(locationReceiver);
        activity.stopService(service);
        mapView.getMapWindow().getMap().getMapObjects().remove(markSartRoute);
        // Сохранить маршрут
        executor.execute(() -> {
            List<Point> points = BufferedRoute.getAll();
            RouteDao rd = AppDatabase.getInstance(activity.getApplication()).getRouteDao();
            RoutePointDao rpd = AppDatabase.getInstance(activity.getApplication()).getRoutePointDao();
            PolygonDao pd = AppDatabase.getInstance(activity.getApplication()).getPolygonDao();
            //сохраняем маршрут и его точки в базу данных
            RouteRepository routeRep = new RouteRepository(rd, rpd);
            routeRep.addNewRoute(routeName, BufferedRoute.getDistance(), -1);
            routeRep.addPointsToRoute(points);
            // Обновляем или создаем новую территорию для этого юзера
            List<com.kyas.wolkandhold.database.entities.Polygon> polygons = pd.getPolygonsByUser(-1);
            if (polygons.isEmpty()) {
                com.kyas.wolkandhold.database.entities.Polygon poly = new com.kyas.wolkandhold.database.entities.Polygon();
                poly.userId = -1;
                poly.lastUpdated = System.currentTimeMillis();
                Gson gson = new Gson();
                poly.pointsJson = gson.toJson(points);
                poly.area = polygonAreaOnEarth(points);
                pd.addPolygon(poly);
            } else {
                double area = polygonAreaOnEarth(points);
                if (area > polygons.get(0).area) {
                    com.kyas.wolkandhold.database.entities.Polygon poly = new com.kyas.wolkandhold.database.entities.Polygon();
                    poly.userId = -1;
                    poly.lastUpdated = System.currentTimeMillis();
                    Gson gson = new Gson();
                    poly.pointsJson = gson.toJson(points);
                    poly.area = polygonAreaOnEarth(points);
                    pd.updatePolygon(poly);
                }
            }
            activity.runOnUiThread(() -> {
                BufferedRoute.clear();
                mapView.getMapWindow().getMap().getMapObjects().remove(recordingPolyline);
                recordingPolyline = null;
                routeViewModel.updatePoints();
                isRecording = false;
            });
            Log.d("DialogSaveRoute", "saved new route with id:" + routeRep.currentRouteId);
        });
    }
    private void stopRecordingWithoutSave(Intent service) {
        btnStartRecording.setIconResource(R.drawable.ic_play);
        btnStartRecording.setText(R.string.start_record);
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(locationReceiver);
        activity.stopService(service);
        mapView.getMapWindow().getMap().getMapObjects().remove(markSartRoute);
        BufferedRoute.clear();
        mapView.getMapWindow().getMap().getMapObjects().remove(recordingPolyline);
        recordingPolyline = null;
        routeViewModel.updatePoints();
        isRecording = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    private void loadPolygons() {
        routeViewModel.getPolygons().observe(getViewLifecycleOwner(), (polygons) -> {
            if (polygons != null) {
                Gson gson = new Gson();
                Type pointListType = new TypeToken<List<Point>>(){}.getType();

                for (com.kyas.wolkandhold.database.entities.Polygon polyEntity : polygons) {

                    List<Point> points = gson.fromJson(polyEntity.pointsJson, pointListType);
                    Polygon polygon = new Polygon(new LinearRing(points), new ArrayList<>());
                    if (polygonsMapObjects.containsKey(polyEntity.userId)) {
                        polygonsMapObjects.get(polyEntity.userId).obj.setGeometry(polygon);
                        continue;

                    }
                    Log.d("TAG", "loadPolygons: " + polyEntity.userId);

                    PolygonMapObject polygonMapObject = mapView.getMapWindow().getMap().getMapObjects().addPolygon(polygon);
                    MapObjectTapListener mapObjectTapListener = new MapObjectTapListener() {
                        @Override
                        public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                            Toast.makeText(activity, "Это ваша территория!", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    };
                    if (polyEntity.userId == -1) {
                        polygonMapObject.addTapListener(mapObjectTapListener);
                        polygonMapObject.setFillColor(Color.argb(100, 255, 0, 0));
                    }
                    polygonsMapObjects.put(polyEntity.userId, new PolygonData(polygonMapObject, mapObjectTapListener));
                }
            }
        });
    }


    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        userLocationView.getPin().setIcon(
                ImageProvider.fromResource(activity, R.drawable.ic_man)
        );
        userLocationView.getArrow().setIcon(
                ImageProvider.fromResource(activity, R.drawable.ic_arrow),
                new IconStyle()
                        .setRotationType(RotationType.ROTATE)
                        .setAnchor(new PointF(0.5f, 0.5f))
                        .setScale(1.5f)
        );
        Log.d("MAPKIT", "onObjectAdded triggered");
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {}

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {

            if (isLocationEnabled()) {

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            routeViewModel.getLocation().setValue(new Point(location.getLatitude(), location.getLongitude()));
                            mapView.getMapWindow().getMap().move(
                                    new CameraPosition(routeViewModel.getLocation().getValue(), 17f, 0, 20),
                                    new Animation(Animation.Type.SMOOTH, 1),
                                    null);
                        }
                    }
                });
            } else {
                Toast.makeText(activity, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(0)
                .setMaxUpdates(1)
                .build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

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
            routeViewModel.getLocation().setValue(new Point(
                    mLastLocation.getLatitude(),
                    mLastLocation.getLongitude()
            ));
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {

        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(activity, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public static double polygonAreaOnEarth(List<Point> coords) {
        if (coords.size() < 3) return 0;

        double total = 0.0;
        int n = coords.size();

        for (int i = 0; i < n; i++) {
            double[] p1 = {coords.get(i).getLatitude(), coords.get(i).getLongitude()};
            double[] p2 = {coords.get((i + 1) % n).getLatitude(), coords.get((i + 1) % n).getLongitude()};

            double lon1 = Math.toRadians(p1[0]);
            double lat1 = Math.toRadians(p1[1]);
            double lon2 = Math.toRadians(p2[0]);
            double lat2 = Math.toRadians(p2[1]);

            total += (lon2 - lon1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }

        double earthRadius = 6378137;
        return Math.abs(total * earthRadius * earthRadius / 2.0);
    }

    static class PolygonData {
        final PolygonMapObject obj;
        final MapObjectTapListener listener;

        public PolygonData(PolygonMapObject obj, MapObjectTapListener listener) {
            this.obj = obj;
            this.listener = listener;
        }
    }
}