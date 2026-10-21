package com.merged.autodexscreen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.view.Display;
import androidx.core.app.NotificationCompat;

public class DisplayWatcherService extends Service implements DisplayManager.DisplayListener {

    private DisplayManager displayManager;
    private static final String CHANNEL_ID = "AutoDeXChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Auto DeX & SecondScreen đang chạy ngầm")
                .setContentText("Đang theo dõi kết nối màn hình ngoài/xe ô tô")
                .setSmallIcon(android.R.drawable.ic_menu_slideshow)
                .build();
        startForeground(1, notification);

        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(this, null);
        }
    }

    @Override
    public void onDisplayAdded(int displayId) {
        if (displayId == Display.DEFAULT_DISPLAY) return;

        SharedPreferences prefs = getSharedPreferences("Config", MODE_PRIVATE);
        String res = prefs.getString("res", "1920x1200");
        String dpi = prefs.getString("dpi", "280");
        String pkg = prefs.getString("pkg", "com.sec.android.app.desktoplauncher/.activity.LauncherActivity");

        ShizukuShell.applyExternalDisplayConfig(displayId, res, dpi, pkg);
    }

    @Override
    public void onDisplayRemoved(int displayId) {
        if (displayId != Display.DEFAULT_DISPLAY) {
            ShizukuShell.resetDisplayConfig();
        }
    }

    @Override
    public void onDisplayChanged(int displayId) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (displayManager != null) {
            displayManager.unregisterDisplayListener(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Auto Display Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
