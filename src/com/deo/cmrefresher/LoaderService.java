package com.deo.cmrefresher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author deo
 */
public class LoaderService extends Service {

    private NotificationManager notifyMgr;
    private String checksum;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        notifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onStart(Intent intent, int startid) {
        String line;
        String[] subprop;
        File root = Environment.getExternalStorageDirectory();

        showNotification("Download in progress");

        try {

            URL u = new URL(intent.getStringExtra("link"));
            String title = intent.getStringExtra("title");
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            Toast.makeText(this, "CMrefresher: download has been started", Toast.LENGTH_LONG).show();

            FileOutputStream f = new FileOutputStream(new File(root, title));

            InputStream in = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();


            java.lang.Process p = Runtime.getRuntime().exec("md5sum " + root.getAbsolutePath() + "/" + title);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = input.readLine();
            input.close();
            subprop = line.split(" ");
            checksum = subprop[0];

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.stopSelf();
    }

    private void showNotification(String message) {
        CharSequence text = "CMrefresher";
        int icon = R.drawable.icon;
        Notification notification = new Notification(icon, text, System.currentTimeMillis());
        Intent intent = new Intent(this, CMrefresher.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        notification.setLatestEventInfo(this, "CMrefresher", message, contentIntent);
        notifyMgr.notify(105, notification);
    }

    @Override
    public void onDestroy() {
        notifyMgr.cancel(105);
        showNotification("Completed. Checksum=" + checksum);
    }
}
