package mx.velian.angurgestion;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

public final class LoginActivity extends Activity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private EditText identifierField;
    private EditText passwordField;
    private TextView messageView;
    private TextView successName;
    private TextView successRole;
    private Button loginButton;
    private View formView;
    private View successView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        View root = findViewById(R.id.rootScroll);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int top;
            int bottom;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            view.setPadding(0, top, 0, bottom);
            return insets;
        });
        root.requestApplyInsets();
        identifierField = findViewById(R.id.identifierField);
        passwordField = findViewById(R.id.passwordField);
        messageView = findViewById(R.id.messageView);
        successName = findViewById(R.id.successName);
        successRole = findViewById(R.id.successRole);
        loginButton = findViewById(R.id.loginButton);
        formView = findViewById(R.id.formView);
        successView = findViewById(R.id.successView);
        loginButton.setOnClickListener(ignored -> submit());
    }

    private void submit() {
        String identifier = identifierField.getText().toString().trim();
        String password = passwordField.getText().toString();
        identifierField.setError(null);
        passwordField.setError(null);
        messageView.setVisibility(View.GONE);

        LoginValidator.Result validation = LoginValidator.validate(identifier, password);
        if (validation != LoginValidator.Result.VALID) {
            if (validation == LoginValidator.Result.MISSING_IDENTIFIER
                    || validation == LoginValidator.Result.MISSING_BOTH) {
                identifierField.setError(getString(R.string.error_identifier));
            }
            if (validation == LoginValidator.Result.MISSING_PASSWORD
                    || validation == LoginValidator.Result.MISSING_BOTH) {
                passwordField.setError(getString(R.string.error_password));
            }
            showMessage(getString(R.string.error_required));
            if (validation == LoginValidator.Result.MISSING_PASSWORD) {
                passwordField.requestFocus();
            } else {
                identifierField.requestFocus();
            }
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText(R.string.validating);
        executor.execute(() -> {
            LoginResponse result = requestLogin(identifier, password);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                loginButton.setEnabled(true);
                loginButton.setText(R.string.login_button);
                if (result.success) {
                    passwordField.setText("");
                    successName.setText(result.name);
                    successRole.setText(result.role.replace('_', ' '));
                    formView.setVisibility(View.GONE);
                    successView.setVisibility(View.VISIBLE);
                } else {
                    passwordField.setText("");
                    showMessage(result.message);
                    passwordField.requestFocus();
                }
            });
        });
    }

    private void showMessage(String message) {
        messageView.setText(message);
        messageView.setVisibility(View.VISIBLE);
        messageView.announceForAccessibility(message);
    }

    private LoginResponse requestLogin(String identifier, String password) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BuildConfig.API_BASE_URL + "/api/auth/login");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            String form = "identifier=" + URLEncoder.encode(identifier, StandardCharsets.UTF_8.name())
                    + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8.name());
            byte[] bytes = form.getBytes(StandardCharsets.UTF_8);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bytes);
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 400
                    ? connection.getInputStream() : connection.getErrorStream();
            StringBuilder body = new StringBuilder();
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                }
            }
            JSONObject json = new JSONObject(body.toString());
            if (status == 200 && json.optBoolean("ok")) {
                return new LoginResponse(true, json.optString("name"), json.optString("role"), "");
            }
            return new LoginResponse(false, "", "",
                    json.optString("message", getString(R.string.error_service)));
        } catch (Exception ex) {
            return new LoginResponse(false, "", "", getString(R.string.error_service));
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private static final class LoginResponse {
        final boolean success;
        final String name;
        final String role;
        final String message;

        LoginResponse(boolean success, String name, String role, String message) {
            this.success = success;
            this.name = name;
            this.role = role;
            this.message = message;
        }
    }
}
