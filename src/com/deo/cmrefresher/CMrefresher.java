package com.deo.cmrefresher;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

            AndroidSaxFeedParser feedParser = new AndroidSaxFeedParser("http://cm-nightlies.appspot.com/rss?device=" + product);
            list = (ArrayList) feedParser.parse();

            Changelog changelog;


            changelog = new Changelog("http://cm-nightlies.appspot.com/changelog/?device=" + product);
            JSONArray nightlies = changelog.getData();

            int i;
            for (i = 0; i < nightlies.length(); i++) {
                JSONObject commit = nightlies.getJSONObject(i);
                String updated = commit.getString("last_updated");
                SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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



            }



            ListAdapter adapter = new ArrayAdapter<Message>(
                    this, R.layout.list, R.id.title, list);

            setListAdapter(adapter);
            ListView lv = getListView();
            lv.setTextFilterEnabled(true);

            lv.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> parentItem, View view, int position, long id) {
                    final Message message = (Message) parentItem.getItemAtPosition(position);

                    AlertDialog alert = new AlertDialog.Builder(CMrefresher.this).setNegativeButton("No", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            Thread t = new Thread() {

                                @Override
                                public void run() {
                                ServiceConnection conn = new ServiceConnection() {
                                    @Override
                                    public void onServiceConnected(ComponentName name, IBinder service) {
                                    }

                                    @Override
                                    public void onServiceDisconnected(ComponentName arg0) {
                                    }
                                };
                                    
                                    Intent downloadIntent = new Intent(getApplicationContext(), LoaderService.class);
                                    downloadIntent.putExtra("title", message.getTitle());
                                    downloadIntent.putExtra("link", message.getLink().toString());

                                    getApplicationContext().bindService(
                                            downloadIntent,
                                            conn,
                                            Context.BIND_AUTO_CREATE);

                                    startService(downloadIntent);
                                }
                            };
                            t.start();
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    }).create();

                    alert.setTitle("Download Now?");
                    alert.setMessage(message.getTitle() + "\n" + message.getDescription());
                    alert.show();

                }
            });
        } catch (Exception e) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Sorry...");
            alertDialog.setMessage(e.getMessage());
            alertDialog.show();
        }
    }
}