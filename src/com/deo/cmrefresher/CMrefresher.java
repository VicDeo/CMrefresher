package com.deo.cmrefresher;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

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
import org.json.JSONArray;
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
        String[] subprop;

        ArrayList<Message> list = new ArrayList<Message>();
        TextView textView = (TextView) findViewById(R.id.status);
        try {
            try {
                java.lang.Process p = Runtime.getRuntime().exec("getprop ro.modversion");
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                line = input.readLine();
                input.close();
                subprop = line.split("-");
                isCyanogen = subprop[0].contentEquals("CyanogenMod") && subprop[1].contentEquals("7");

                if (isCyanogen) {
                    buildType = subprop[3];
                    SimpleDateFormat inputFormatter = new SimpleDateFormat("MMddyyyy");
                    buildDate = inputFormatter.parse(subprop[2]);
                    product = subprop[4].toLowerCase();

                    SimpleDateFormat outputFormatter = new SimpleDateFormat("dd-MMM-yyyy");
                    textView.setText("Cyanogen 7 " + buildType + " build on " + outputFormatter.format(buildDate) + " for " + product);
                } else {
                    textView.setText("The current firmware is not Cyanogen 7");
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            AndroidSaxFeedParser feedParser = new AndroidSaxFeedParser("http://cm-nightlies.appspot.com/rss?device=" + product);
            list = (ArrayList) feedParser.parse();

            Changelog changelog;

            try {
                changelog = new Changelog("http://cm-nightlies.appspot.com/changelog/?device=" + product);
                JSONArray nightlies = changelog.getData();

                int i;
                for (i = 0; i < nightlies.length(); i++) {
                    JSONObject commit = nightlies.getJSONObject(i);
                    String updated = commit.getString("last_updated");
                    SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date updatedAt = inputFormatter.parse(updated.trim());
                        Message msg = new Message();
                        Message targetMsg = new Message();
                        Iterator iter = list.iterator();

                        while (iter.hasNext()) {
                            msg = (Message) iter.next();
                            if (updatedAt.after(msg.getDateObj())) {
                                break;
                            }
                            targetMsg = msg;
                        }

                        if (targetMsg != msg) {
                            targetMsg.setDescription(targetMsg.getDescription()
                                    + "\n" + commit.getString("project") + ": "
                                    + commit.getString("subject") + "\n");
                        }

                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ListAdapter adapter = new ArrayAdapter<Message>(
                    this, R.layout.list, R.id.title, list);

            setListAdapter(adapter);
            ListView lv = getListView();
            lv.setTextFilterEnabled(true);

            lv.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Message message = (Message) parent.getItemAtPosition(position);

                    AlertDialog alert = new AlertDialog.Builder(CMrefresher.this).setNegativeButton("No", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            Loader loader = new Loader(message.getLink(), message.getTitle());

                        }
                    }).create();

                    alert.setTitle("Download Now?");
                    alert.setMessage(message.getTitle() + "\n" + message.getDescription());
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