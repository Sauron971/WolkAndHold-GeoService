package com.kyas.wolkandhold.ui.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.kyas.wolkandhold.ui.data.model.LoggedInUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    private SharedPreferences sharedPreferences;
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(Context context, LoginDataSource dataSource) {
        this.dataSource = dataSource;
        this.sharedPreferences = context.getApplicationContext()
                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    public static LoginRepository getInstance(Context context, LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(context, dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        String token = sharedPreferences.getString("token", "0");

        if (token != null && !token.isEmpty() && !token.equals("0")) {
            return true;
        }

        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public Result<LoggedInUser> login(String username, String password) {
        // handle login
        Result<LoggedInUser> result = dataSource.login(username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token", user.getJwtToken());
            editor.apply();
        }

        return result;
    }
}