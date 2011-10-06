package com.deo.cmrefresher;

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

        try {
            TextView textView = (TextView) findViewById(R.id.status);

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
                        }
                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    }).create();

                    alert.setTitle("Download Now?");
                    alert.setMessage(message.getTitle() + "\n" + message.getDate()+"\n"+ message.getDescription());
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