package com.deo.cmrefresher;

import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author deo
 */
public class Loader {

    public Loader(URL u, String title) {
        try {
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            File root = Environment.getExternalStorageDirectory();
            FileOutputStream f = new FileOutputStream(new File(root, title));

            InputStream in = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
