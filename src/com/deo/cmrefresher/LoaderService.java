package com.deo.cmrefresher;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author deo
 */
public class LoaderService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onStart(Intent intent, int startid) {
        try {

            URL u = new URL(intent.getStringExtra("link"));
            String title = intent.getStringExtra("title");
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            Toast.makeText(this, "CMrefresher: download has been started", Toast.LENGTH_LONG).show();
            File root = Environment.getExternalStorageDirectory();
            FileOutputStream f = new FileOutputStream(new File(root, title));

            InputStream in = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
            this.stopSelf();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "CMrefresher: download has been completed", Toast.LENGTH_LONG).show();
    }
}
