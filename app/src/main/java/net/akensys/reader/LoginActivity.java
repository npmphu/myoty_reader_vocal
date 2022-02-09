package net.akensys.reader;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import net.akensys.reader.model.User;
import net.akensys.reader.service.AKRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private ProgressBar mProgressBar;
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar = findViewById(R.id.pb_login);
        mUserNameEditText = findViewById(R.id.et_username);
        String username = PrefsHelper.getLogin(getApplicationContext());
        if (username != null && !username.isEmpty()) {
            mUserNameEditText.setText(username);
        }
        mPasswordEditText = findViewById(R.id.et_password);
        String password = PrefsHelper.getPassword(getApplicationContext());
        if (password != null && !password.isEmpty()) {
            mPasswordEditText.setText(password);
        }
        Button mLoginButton = findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(view -> login());
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
        finishAffinity();
        System.exit(0);
    }

    private void login() {
        String username = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            AlertDialog alertDialog = new AlertDialog
                    .Builder(LoginActivity.this)
                    .setTitle("Email invalide")
                    .setMessage("Email ne peut pas être vide. Veuillez saisir votre adresse email pour vous connecter à MyOty.")
                    .setNeutralButton(R.string.OK, (dialog, which) -> dialog.dismiss())
                    .create();
            alertDialog.show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            AlertDialog alertDialog = new AlertDialog
                    .Builder(LoginActivity.this)
                    .setTitle("Email invalide")
                    .setMessage("Le format de votre adresse email n'est pas valide. Veuillez saisir votre adresse email pour vous connecter à MyOty.")
                    .setNeutralButton(R.string.OK, (dialog, which) -> dialog.dismiss())
                    .create();
            alertDialog.show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            AlertDialog alertDialog = new AlertDialog
                    .Builder(LoginActivity.this)
                    .setTitle("Mot de passe invalid")
                    .setMessage("Mot de passe ne peut pas être vide. Veuillez saisir votre mot de passe pour vous connecter à MyOty.")
                    .setNeutralButton(R.string.OK, (dialog, which) -> dialog.dismiss())
                    .create();
            alertDialog.show();
            return;
        }
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        String url = Reference.BASE_URL + Reference.API_LOGIN_CONNECT;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("username", username);
            jsonRequest.put("password", password);
        } catch (JSONException ignored) { }
        JsonObjectRequest loginConnect = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonRequest,
                response -> {
                    try {
                        String token = response.getString("token");
                        MyOty.getInstance().setToken(token);
                        PrefsHelper.setToken(getApplicationContext(), token);
                        PrefsHelper.setLogin(getApplicationContext(), username);
                        PrefsHelper.setPassword(getApplicationContext(), password);
                    } catch (JSONException ignored) { }
                    fetchUserSession();
                },
                error -> {
                    mProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    AKRequest.solveErrorResponse(getApplicationContext(), error);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        MyOty.getInstance().addToRequestQueue(getApplicationContext(), loginConnect);
    }

    private void fetchUserSession() {
        String url = Reference.BASE_URL + Reference.API_GET_SESSION;
        JsonObjectRequest userSession = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    User user = new Gson().fromJson(String.valueOf(response), User.class);
                    MyOty.getInstance().setCurrentUser(user);
                    PrefsHelper.setUserUUID(getApplicationContext(), user.getUuid());
                    PrefsHelper.setReaderName(getApplicationContext(), user.getNom() + " " + user.getPrenom());
                    PrefsHelper.setReaderType(getApplicationContext(), user.getReaderType());
                    PrefsHelper.setClientUUID(getApplicationContext(), user.getClient().getUuid());
                    mProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    onAuthenticationSuccess();
                },
                error -> {
                    mProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    AKRequest.solveErrorResponse(getApplicationContext(), error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; utf-8");
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + PrefsHelper.getToken(getApplicationContext()));
                return headers;
            }
        };
        MyOty.getInstance().addToRequestQueue(getApplicationContext(), userSession);
    }

    private void onAuthenticationSuccess() {
        startNetworkActivity();
    }

    private void startNetworkActivity() {
        Intent intent = new Intent(this, NetworkActivity.class);
        startActivity(intent);
    }
}