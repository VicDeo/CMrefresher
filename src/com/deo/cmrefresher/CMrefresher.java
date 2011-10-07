package com.deo.cmrefresher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import java.text.SimpleDateFormat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;

import android.widget.TextView;
import android.widget.ListView;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CMrefresher extends ListActivity {

    String product;
    Date buildDate;
    String buildType;
    boolean isCyanogen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String line;
        String[] prop, subprop;

        ArrayList<Message> list = new ArrayList<Message>();
        TextView textView = (TextView) findViewById(R.id.status);
        try {


            java.lang.Process p = Runtime.getRuntime().exec("getprop");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {

                prop = line.split(": ");
                if (prop[0].contentEquals("[ro.modversion]")) {
                    prop[1] = prop[1].substring(1, prop[1].length() - 1);
                    subprop = prop[1].split("-");
                    isCyanogen = subprop[0].contentEquals("CyanogenMod") && subprop[1].contentEquals("7");
                    buildType = subprop[3];
                    SimpleDateFormat inputFormatter = new SimpleDateFormat("MMddyyyy");
                    buildDate = inputFormatter.parse(subprop[2]);
                    product = subprop[4].toLowerCase();
                }
            }
            input.close();
            if (isCyanogen) {
                SimpleDateFormat outputFormatter = new SimpleDateFormat("dd-MMM-yyyy");
                textView.setText("Cyanogen 7 " + buildType + " build on " + outputFormatter.format(buildDate) + " for " + product);
            }
        } catch (Exception err) {
            System.out.println(err.toString());
        }

        try {

            AndroidSaxFeedParser feedParser = new AndroidSaxFeedParser("http://cm-nightlies.appspot.com/rss?device=" + product);
            list = (ArrayList) feedParser.parse();


            Changelog changelog;
            try {
                changelog = new Changelog("http://cm-nightlies.appspot.com/changelog/?device=" + product);
                JSONArray nightlies = changelog.getData();

                int i;
                Iterator iter = list.iterator();
                for (i = 0; i < nightlies.length(); i++) {
                    JSONObject commit = nightlies.getJSONObject(i);
                    String updated = commit.getString("last_updated");
                    SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date updatedAt = inputFormatter.parse(updated.trim());
                        textView.setText(updatedAt.toString());
                        Message msg = (Message) iter.next();
                        Message msgPrev;
                        /*
                        while (iter.hasNext()) {
                            msgPrev = msg;
                            if (iter.hasNext()) {
                                msg = (Message) iter.next();
                            }
                            if (updatedAt.after(msg.getDateObj())) {
                                break;
                            }

                            if (updatedAt.after(msgPrev.getDateObj())) {
                                msg.setDescription(msg.getDescription() + "\n" + commit.getString("project") + ": " + commit.getString("subject"));
                            }

                        } */
                    } catch (ParseException ex) {
                        Logger.getLogger(CMrefresher.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            } catch (MalformedURLException ex) {
                Logger.getLogger(CMrefresher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(CMrefresher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                Logger.getLogger(CMrefresher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CMrefresher.class.getName()).log(Level.SEVERE, null, ex);
            }



            ListAdapter adapter = new ArrayAdapter<Message>(
                    this, R.layout.list, R.id.title, list);

            setListAdapter(adapter);
            ListView lv = getListView();
            lv.setTextFilterEnabled(true);

            lv.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Message message = (Message) parent.getItemAtPosition(position);

                    AlertDialog alert = new AlertDialog.Builder(CMrefresher.this).setNegativeButton("No", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                            dialog.dismiss();
                        }
                    }).create();

                    alert.setTitle("Download Now?");
                    alert.setMessage(message.getTitle() + "\n" + message.getDate() + "\n" + message.getDescription());
                    alert.show();

                }
            });
        } catch (RuntimeException e) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Sorry...");
            alertDialog.setMessage(e.getMessage());
            alertDialog.show();
        }
    }
}