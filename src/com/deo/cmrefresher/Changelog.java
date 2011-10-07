package com.deo.cmrefresher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author deo
 */
public class Changelog {

    private final URI feedUrl;

    public Changelog(String feedUrla) throws MalformedURLException, URISyntaxException {
        this.feedUrl = new URI(feedUrla);
    }

    public JSONArray getData() throws JSONException, IOException {
        HttpGet get = new HttpGet(feedUrl);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(get);

        return (JSONArray) new JSONTokener(request(response)).nextValue();
    }

    public static String request(HttpResponse response) {
        String result = "";
        try {
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");
            }
            in.close();
            result = str.toString();
        } catch (Exception ex) {
            result = "Error";
        }
        return result;
    }
}
