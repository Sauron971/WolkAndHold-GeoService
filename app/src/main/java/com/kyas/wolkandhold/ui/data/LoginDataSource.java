package com.kyas.wolkandhold.ui.data;

import android.util.Log;

import com.kyas.wolkandhold.BuildConfig;
import com.kyas.wolkandhold.data.api.ApiService;
import com.kyas.wolkandhold.data.api.requests.LoginRequest;
import com.kyas.wolkandhold.data.api.response.AuthResponse;
import com.kyas.wolkandhold.ui.data.model.LoggedInUser;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private final Retrofit retrofit;
    private final ApiService apiService;

    public LoginDataSource() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public Result<LoggedInUser> login(String username, String password) {

        try {
            Log.d("LOGINLOGIN", "login: trying logged in");
            Response<AuthResponse> resp = apiService.login(new LoginRequest(username, password)).execute();
            Log.d("LOGINLOGIN", "login: " + resp.isSuccessful());
            Log.d("LOGINLOGIN", "login1: " + resp.body());
            if (resp.isSuccessful() && resp.body() != null) {
                LoggedInUser fakeUser =
                        new LoggedInUser(
                                java.util.UUID.randomUUID().toString(),
                                username,
                                resp.body().getToken());
                Log.d("LOGINLOGIN", "login: " + resp.body());
                Log.d("LOGINLOGIN", "login: " + resp.code());
                return new Result.Success<>(fakeUser);
            } else {
                return new Result.Error(new Exception("Error login"));
            }

        } catch (Exception e) {
            Log.e("ERROR LOGIN", "Crashed login here: ", e);
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}