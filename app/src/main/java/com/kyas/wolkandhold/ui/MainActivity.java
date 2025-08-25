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
    private Fragment[] activeFragment;

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
        Fragment mapFragment = new MapFragment();
        Fragment routesFragment = new RoutesFragment();
        activeFragment = new Fragment[]{mapFragment};

        fm.beginTransaction()
                .add(R.id.nav_host_fragment, routesFragment, "ROUTES")
                .hide(routesFragment)
                .commit();

        fm.beginTransaction()
                .add(R.id.nav_host_fragment, mapFragment, "MAP")
                .commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int menuId = menuItem.getItemId();
                if (menuId == R.id.mapFragment) {
                    fm.beginTransaction()
                            .hide(activeFragment[0])
                            .show(mapFragment)
                            .commit();
                    activeFragment[0] = mapFragment;
                    return true;
                } else if (menuId == R.id.routesFragment) {
                    fm.beginTransaction()
                            .hide(activeFragment[0])
                            .show(routesFragment)
                            .commit();
                    activeFragment[0] = routesFragment;
                    return true;
                }
                return false;
            }
        });
        SharedPreferences sett = getSharedPreferences("token", MODE_PRIVATE);
        SharedPreferences.Editor editor = sett.edit();
        editor.putString("jwt", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTYXVyb24iLCJpYXQiOjE3NTU5NTY1MDYsImV4cCI6MTc1ODYzNDkwNn0.Vv64YPdn0eQGOdm7W_vIkk6mk8oxHrvl-QCuhBG99TQ");
        editor.apply();
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