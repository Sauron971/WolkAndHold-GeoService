package com.kyas.wolkandhold.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationBarView;
import com.kyas.wolkandhold.R;
import com.kyas.wolkandhold.data.RouteViewModel;
import com.kyas.wolkandhold.ui.leaderboard.RecyclerLeadersAdapter;
import com.kyas.wolkandhold.ui.mapfragment.MapFragment;
import com.kyas.wolkandhold.ui.routesfragment.RoutesFragment;
import com.yandex.mapkit.MapKitFactory;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fm;
    private Fragment mapFragment;
    private Fragment routesFragment;
    private Fragment active;
    private RecyclerView recyclerViewLeaderboard;
    private RouteViewModel routeViewModel;

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
        routeViewModel = new ViewModelProvider(this).get(RouteViewModel.class);

        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_nav);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
        fm = getSupportFragmentManager();
        RecyclerLeadersAdapter recyclerAdapter = new RecyclerLeadersAdapter(this);
        recyclerViewLeaderboard = findViewById(R.id.leaderboard_recycler);
        recyclerViewLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLeaderboard.setAdapter(recyclerAdapter);

        routeViewModel.getLeaderboard().observe(this, recyclerAdapter::setLeaders);
        routeViewModel.loadLeaderboard();

        FrameLayout llBottomSheet = (FrameLayout) findViewById(R.id.bottom_sheet_leaders);
        BottomSheetBehavior<View> bottomSheetLeaders = BottomSheetBehavior.from(llBottomSheet);

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

            if (menuId == R.id.bottomSheetLeaders) {
                bottomSheetLeaders.setState(BottomSheetBehavior.STATE_EXPANDED);
                return true;
            }

            bottomSheetLeaders.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (menuId == R.id.mapFragment) {
                if (active == mapFragment) return true;
                fm.beginTransaction().hide(active).show(mapFragment).commit();
                active = mapFragment;
                return true;
            } else if (menuId == R.id.routesFragment) {
                if (active == routesFragment) return true;
                fm.beginTransaction().hide(active).show(routesFragment).commit();
                active = routesFragment;
                return true;
            }
            return false;
        });
        bottomSheetLeaders.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (i == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomNavigationView.getMenu().findItem(R.id.bottomSheetLeaders).setChecked(true);
                    routeViewModel.loadLeaderboard();
                }
                else if (i == BottomSheetBehavior.STATE_COLLAPSED || i == BottomSheetBehavior.STATE_HIDDEN) {
                    if (active == mapFragment) {
                        bottomNavigationView.getMenu().findItem(R.id.mapFragment).setChecked(true);
                    } else if (active == routesFragment) {
                        bottomNavigationView.getMenu().findItem(R.id.routesFragment).setChecked(true);
                    }
                }

            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
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