package com.kyas.wolkandhold.data.api;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.kyas.wolkandhold.BuildConfig;
import com.kyas.wolkandhold.data.api.requests.LocationRequest;
import com.kyas.wolkandhold.data.api.response.PolygonResponse;
import com.kyas.wolkandhold.data.api.response.UserResponse;
import com.kyas.wolkandhold.data.models.PlayerModel;
import com.kyas.wolkandhold.data.database.entities.Polygon;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class GameSocketManager {
    private StompClient stompClient;
    private final Gson gson = new Gson();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public interface OnPolygonReceivedListener {
        void onNewPolygons(List<Polygon> polygons);
    }

    private OnPolygonReceivedListener listenerPolygon;

    public void setPolygonListener(OnPolygonReceivedListener listener) {
        this.listenerPolygon = listener;
    }

    public interface OnPlayerReceivedListener {
        void onNewPlayer(PlayerModel player);
    }

    private OnPlayerReceivedListener listenerPlayer;

    public void setPlayerListener(OnPlayerReceivedListener listener) {
        this.listenerPlayer = listener;
    }

    public void connect(String jwt) {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BuildConfig.WS_URL);

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", "Bearer " + jwt));

        // Мониторинг состояния подключения
        Disposable lifecycle = stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("WS", "Connected");
                    subscribeToPolygons();
                    subscribeToLocationsOfPlayer();

                    break;
                case ERROR:
                    Log.e("WS", "Error", lifecycleEvent.getException());
                    new Handler(Looper.getMainLooper()).postDelayed(() -> connect(jwt), 5000);
                    break;
                case CLOSED:
                    Log.d("WS", "Closed");
                    break;
            }
        });
        Log.d("WS_DEBUG", "Connecting to ws with jwt: " + jwt);
        compositeDisposable.add(lifecycle);
        stompClient.connect(headers);
    }

    // Подписка на личную очередь (получение захваченных зон)
    private void subscribeToPolygons() {
        Disposable topic = stompClient.topic("/user/queue/polygons")
                .subscribe(msg -> {
                    PolygonResponse response = gson.fromJson(msg.getPayload(), PolygonResponse.class);
                    if (listenerPolygon != null) {
                        listenerPolygon.onNewPolygons(response.toEntities());
                    }
                }, err -> Log.e("WS", "Topic error", err));

        compositeDisposable.add(topic);
    }

    private void subscribeToLocationsOfPlayer() {
        Disposable topic = stompClient.topic("/topic/all_players")
                .subscribe(msg -> {
                    UserResponse response = gson.fromJson(msg.getPayload(), UserResponse.class);
                    Log.d("WS_PLAYER", "from server: userId=" + response.getUserId()
                            + " lat=" + response.getLat()
                            + " lon=" + response.getLon()
                            + " isCapture=" + response.isCapture());
                    if (listenerPlayer != null) {
                        listenerPlayer.onNewPlayer(new PlayerModel(
                                response.getUserId(),
                                response.getUsername(),
                                response.getLat(),
                                response.getLon(),
                                response.isCapture(),
                                System.currentTimeMillis()));
                    }
                }, err -> Log.e("WS", "Topic error", err));
        compositeDisposable.add(topic);
    }

    @SuppressLint("CheckResult")
    public void sendLocation(double lat, double lon, boolean isCapture) {
        if (stompClient != null && stompClient.isConnected()) {
            // Создаем простой объект для отправки
            LocationRequest request = new LocationRequest(lat, lon, isCapture);
            String json = gson.toJson(request);

            stompClient.send("/app/move", json)
                    .subscribe(() -> {},
                            err -> Log.e("WS", "Send error", err));
        }
    }

    public void disconnect() {
        if (compositeDisposable != null) compositeDisposable.dispose();
        if (stompClient != null) stompClient.disconnect();
    }
}
