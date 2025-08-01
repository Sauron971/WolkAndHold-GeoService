package com.kyas.wolkandhold;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.yandex.mapkit.geometry.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LocationRecordService extends Service {

    private LocationListener locationListener;
    private LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = this::sendLocationUpdate;
        Log.d("LocationService", "onCreate: startingForeground");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationService", "Нет разрешений на GPS");
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                1,
                locationListener
        );
        startForeground(1, createNotification());
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
                "location_channel",
                "GPS Tracking",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        Log.d("LocationService", "createNotification: creating");
        return new NotificationCompat.Builder(this, "location_channel")
                .setContentTitle("Запись маршрута")
                .setContentText("Идёт отслеживание GPS")
                .setSmallIcon(R.drawable.ic_pin)
                .build();
    }

    private void sendLocationUpdate(Location location) {
        Intent intent = new Intent("LOCATION_UPDATE");
        Log.d("LocationService", "Broadcast sent: " + location.getLatitude() + ", " + location.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        BufferedRoute.add(new Point(location.getLatitude(), location.getLongitude()));
    }
}
