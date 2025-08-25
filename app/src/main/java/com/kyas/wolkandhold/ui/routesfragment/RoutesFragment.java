package com.kyas.wolkandhold.ui.routesfragment;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kyas.wolkandhold.R;
import com.kyas.wolkandhold.data.RouteViewModel;
import com.kyas.wolkandhold.data.database.AppDatabase;
import com.kyas.wolkandhold.data.database.dao.RoutePointDao;
import com.kyas.wolkandhold.data.database.entities.Route;
import com.yandex.mapkit.geometry.Point;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoutesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoutesFragment extends Fragment {

    private RecyclerView recyclerViewRoutes;
    private TextView textViewEmptyRoutes;
    private Activity activity;
    private Executor executor;
    private RouteViewModel routeViewModel;

    public RoutesFragment() {
        // Required empty public constructor
    }

    public static RoutesFragment newInstance() {
        return new RoutesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = requireActivity();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewRoutes = view.findViewById(R.id.routes_recycler);
        textViewEmptyRoutes = view.findViewById(R.id.text_empty_routes);
        executor = Executors.newSingleThreadExecutor();
        RecyclerRoutesAdapter adapterRecycler = getRecyclerRoutesAdapter();
        recyclerViewRoutes.setAdapter(adapterRecycler);
        ItemTouchHelper helper = new ItemTouchHelper(new RouteSwipeCallback(adapterRecycler));
        helper.attachToRecyclerView(recyclerViewRoutes);

        routeViewModel = new ViewModelProvider(requireActivity()).get(RouteViewModel.class);
        routeViewModel.getRoutes().observe(getViewLifecycleOwner(), (routes) -> {
            if (routes.isEmpty()) {
                textViewEmptyRoutes.setVisibility(View.VISIBLE);
            } else {
                textViewEmptyRoutes.setVisibility(View.GONE);
            }
            adapterRecycler.setRoutes(routes);
        });

    }

    @NonNull
    private RecyclerRoutesAdapter getRecyclerRoutesAdapter() {
        RecyclerRoutesAdapter.onRouteClickListener onRouteClickListener = new RecyclerRoutesAdapter.onRouteClickListener() {
            @Override
            public void onRouteClick(Route route, int position) {
                executor.execute(() -> {
                    Log.d("RouteClicked", "Clicked route on recycler " + route.toString());
                    RoutePointDao dao = AppDatabase.getInstance(activity.getApplication()).getRoutePointDao();
                    dao.getPointsForRoute(route.id).forEach((p) -> {
                        routeViewModel.getPoints().getValue().add(new Point(p.latitude, p.longitude));
                    });
                });

            }

            @Override
            public void onDeleteButtonClick(Route route, int position) {
                routeViewModel.deleteRoute(route.id);
                Log.d("DeleteRoute", "deleting " + route.name);
            }

        };
        return new RecyclerRoutesAdapter(activity, onRouteClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_routes, container, false);
    }


}