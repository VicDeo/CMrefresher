package com.deo.cmrefresher;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.InputStreamReader;


import android.app.ListActivity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import android.widget.TextView;

public class CMrefresher extends ListActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        String line; 
        
        ArrayList<ContactItem> list = new ArrayList<ContactItem>();
        
         try 
        { 
            TextView textView = (TextView) findViewById(R.id.line);

           ArrayList<String> processList = new ArrayList<String>(); 
            java.lang.Process p = Runtime.getRuntime().exec("getprop"); 
            BufferedReader input =   new BufferedReader(new InputStreamReader(p.getInputStream())); 
            while ((line = input.readLine()) != null) 
           { 
                processList.add(line); 
                if (line.startsWith("[ro.modversion]: [CyanogenMod-7")){
                    line.replaceFirst("[ro.modversion]: [", "");
                    textView.setText(line);
                }
            } 
            input.close(); 
   
        } 
         catch (Exception err) 
        { 
        } 
        
        try {     
          AndroidSaxFeedParser feedParser = new AndroidSaxFeedParser("http://cm-nightlies.appspot.com/rss?device=legend");
          List<Message> feed = feedParser.parse();
          ListIterator itr = feed.listIterator(); 
          while(itr.hasNext()) {
              Message message = (Message) itr.next();
              list.add(new ContactItem(message.getTitle(), message.getDate()));
          }
          
          ListAdapter adapter = new SimpleAdapter(
                this, list, R.layout.list,
                new String[] {ContactItem.TITLE, ContactItem.DATE},
                new int[] {R.id.title, R.id.date});    
        
          setListAdapter(adapter);
        } catch (RuntimeException e){
          AlertDialog alertDialog = new AlertDialog.Builder(this).create();
          alertDialog.setTitle("Sorry...");
          alertDialog.setMessage(e.getMessage());
          alertDialog.show();   
        }
    }
}