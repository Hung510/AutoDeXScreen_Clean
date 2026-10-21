package com.merged.autodexscreen;

import java.io.OutputStream;
import java.lang.reflect.Method;
import rikka.shizuku.Shizuku;

public class ShizukuShell {

    public static void execCommands(String[] commands) {
        if (!Shizuku.pingBinder()) return;

        new Thread(() -> {
            try {
                Method newProcessMethod = Shizuku.class.getMethod("newProcess", String[].class, String[].class, String.class);
                newProcessMethod.setAccessible(true);
                Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh"}, null, null);

                OutputStream os = process.getOutputStream();
                for (String cmd : commands) {
                    os.write((cmd + "\n").getBytes());
                    os.flush();
                }
                os.write("exit\n".getBytes());
                os.flush();
                os.close();
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void applyExternalDisplayConfig(int displayId, String resolution, String dpi, String targetPackage) {
        String[] cmds = new String[]{
            "wm size " + resolution + " -d " + displayId,
            "wm density " + dpi + " -d " + displayId,
            "wm user-rotation -d " + displayId + " lock 1",
            "settings put global force_desktop_mode_on_external_displays 1",
            "settings put global enable_freeform_support 1",
            "sleep 0.5",
            "am start --display " + displayId + " " + targetPackage
        };
        execCommands(cmds);
    }

    public static void resetDisplayConfig() {
        String[] cmds = new String[]{
            "wm size reset",
            "wm density reset"
        };
        execCommands(cmds);
    }
}
