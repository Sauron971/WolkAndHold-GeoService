package com.kyas.wolkandhold.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationBarView;
import com.kyas.wolkandhold.R;
import com.kyas.wolkandhold.ui.mapfragment.MapFragment;
import com.kyas.wolkandhold.ui.routesfragment.RoutesFragment;
import com.yandex.mapkit.MapKitFactory;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fm;
    private Fragment mapFragment;
    private Fragment routesFragment;
    private Fragment active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_nav);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
        fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            mapFragment = new MapFragment();
            routesFragment = new RoutesFragment();

            fm.beginTransaction()
                    .add(R.id.nav_host_fragment, routesFragment, "ROUTES").hide(routesFragment)
                    .add(R.id.nav_host_fragment, mapFragment, "MAP")
                    .commit();
            active = mapFragment;
        } else {
            // ПОВТОРНЫЙ ЗАПУСК: Ищем уже существующие фрагменты по тегам
            mapFragment = fm.findFragmentByTag("MAP");
            routesFragment = fm.findFragmentByTag("ROUTES");

            // Пытаемся понять, какой из них был активным до пересоздания
            // Если карта не скрыта, значит она активна
            if (mapFragment.isHidden()) {
                active = routesFragment;
            } else {
                active = mapFragment;
            }
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int menuId = item.getItemId();
            if (menuId == R.id.mapFragment) {
                if (active == mapFragment) return true; // Уже тут
                fm.beginTransaction().hide(active).show(mapFragment).commit();
                active = mapFragment;
                return true;
            } else if (menuId == R.id.routesFragment) {
                if (active == routesFragment) return true; // Уже тут
                fm.beginTransaction().hide(active).show(routesFragment).commit();
                active = routesFragment;
                return true;
            }
            return false;
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
    }

    @Override
    protected void onStop() {
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}