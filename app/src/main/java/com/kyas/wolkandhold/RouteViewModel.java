package com.kyas.wolkandhold;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RouteViewModel extends ViewModel {
    private final MutableLiveData<List<Point>> _points = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Point>> points = _points;

    public void updatePoints() {
        List<Point> current = BufferedRoute.getAll();
        _points.setValue(current);
    }

    public void clear() {
        _points.setValue(new ArrayList<>());
    }
}
