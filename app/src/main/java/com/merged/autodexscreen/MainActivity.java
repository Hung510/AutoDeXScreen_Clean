package com.merged.autodexscreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private EditText resInput, dpiInput, pkgInput;

    private final Shizuku.OnRequestPermissionResultListener PERMISSION_LISTENER = (requestCode, grantResult) -> {
        updateShizukuStatus();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Shizuku.addRequestPermissionResultListener(PERMISSION_LISTENER);
        } catch (Throwable ignored) {}

        SharedPreferences prefs = getSharedPreferences("Config", MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        statusText = new TextView(this);
        statusText.setTextSize(15f);
        layout.addView(statusText);

        TextView labelPreset = new TextView(this);
        labelPreset.setText("Preset chọn nhanh cho màn hình xe ô tô:");
        labelPreset.setTextSize(14f);
        layout.addView(labelPreset);

        LinearLayout presetLayout = new LinearLayout(this);
        presetLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button btn1200 = new Button(this);
        btn1200.setText("1920x1200 (DPI 280)");
        btn1200.setOnClickListener(v -> {
            resInput.setText("1920x1200");
            dpiInput.setText("280");
        });

        Button btnFHD = new Button(this);
        btnFHD.setText("FullHD (DPI 260)");
        btnFHD.setOnClickListener(v -> {
            resInput.setText("1920x1080");
            dpiInput.setText("260");
        });

        presetLayout.addView(btn1200);
        presetLayout.addView(btnFHD);
        layout.addView(presetLayout);

        resInput = new EditText(this);
        resInput.setHint("Độ phân giải (VD: 1920x1200)");
        resInput.setText(prefs.getString("res", "1920x1200"));
        layout.addView(resInput);

        dpiInput = new EditText(this);
        dpiInput.setHint("Mật độ DPI (Cho xe: 280 - 320)");
        dpiInput.setText(prefs.getString("dpi", "280"));
        layout.addView(dpiInput);

        pkgInput = new EditText(this);
        pkgInput.setHint("Package Launcher / DeX");
        pkgInput.setText(prefs.getString("pkg", "com.sec.android.app.desktoplauncher/.activity.LauncherActivity"));
        layout.addView(pkgInput);

        Button btnSaveAndStart = new Button(this);
        btnSaveAndStart.setText("Bật Tự động chạy ngầm khi cắm màn hình");
        btnSaveAndStart.setOnClickListener(v -> {
            prefs.edit()
                    .putString("res", resInput.getText().toString().trim())
                    .putString("dpi", dpiInput.getText().toString().trim())
                    .putString("pkg", pkgInput.getText().toString().trim())
                    .apply();

            Intent serviceIntent = new Intent(this, DisplayWatcherService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "Đã khởi chạy dịch vụ ngầm thành công!", Toast.LENGTH_SHORT).show();
        });
        layout.addView(btnSaveAndStart);

        Button btnReset = new Button(this);
        btnReset.setText("Khôi phục màn hình gốc (Reset)");
        btnReset.setOnClickListener(v -> ShizukuShell.resetDisplayConfig());
        layout.addView(btnReset);

        setContentView(layout);
        checkAndRequestShizuku();
    }

    private void checkAndRequestShizuku() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(1001);
                }
            }
        } catch (Throwable ignored) {}
        updateShizukuStatus();
    }

    private void updateShizukuStatus() {
        try {
            if (!Shizuku.pingBinder()) {
                statusText.setText("Trạng thái Shizuku: CHƯA CHẠY");
            } else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                statusText.setText("Trạng thái Shizuku: ĐÃ CẤP QUYỀN SẴN SÀNG");
            } else {
                statusText.setText("Trạng thái Shizuku: CHƯA CẤP QUYỀN");
            }
        } catch (Throwable e) {
            statusText.setText("Trạng thái Shizuku: Lỗi kết nối");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Shizuku.removeRequestPermissionResultListener(PERMISSION_LISTENER);
        } catch (Throwable ignored) {}
    }
}
