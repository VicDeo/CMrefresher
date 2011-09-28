package com.deo.cmrefresher;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Date;

import java.text.SimpleDateFormat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import android.widget.TextView;

public class CMrefresher extends ListActivity {

    String product;
    Date buildDate;
    String buildType;
    boolean isCyanogen;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String line;
        String[] prop, subprop;
        

        ArrayList<ContactItem> list = new ArrayList<ContactItem>();

        try {
            TextView textView = (TextView) findViewById(R.id.status);

            ArrayList<String> processList = new ArrayList<String>();
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
                    processList.add(line);
                }
/*                if (prop[0].contentEquals("[ro.build.product]")) {
                    product = prop[1].substring(1, prop[1].length() - 1);
                }
  */
            }
            input.close();
            if (isCyanogen){
              SimpleDateFormat outputFormatter = new SimpleDateFormat("dd-MMM-yyyy");
              textView.setText("Cyanogen 7 " + buildType + " build on " + outputFormatter.format(buildDate) + " for " + product);
            }
        } catch (Exception err) {
            System.out.println(err.toString());
        }

        try {
            AndroidSaxFeedParser feedParser = new AndroidSaxFeedParser("http://cm-nightlies.appspot.com/rss?device="+ product);
            List<Message> feed = feedParser.parse();
            ListIterator itr = feed.listIterator();
            while (itr.hasNext()) {
                Message message = (Message) itr.next();
                list.add(new ContactItem(message.getTitle(), message.getDate()));
            }

            ListAdapter adapter = new SimpleAdapter(
                    this, list, R.layout.list,
                    new String[]{ContactItem.TITLE, ContactItem.DATE},
                    new int[]{R.id.title, R.id.date});

            setListAdapter(adapter);
        } catch (RuntimeException e) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Sorry...");
            alertDialog.setMessage(e.getMessage());
            alertDialog.show();
        }
    }
}