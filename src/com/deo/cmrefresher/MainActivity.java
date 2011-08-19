package com.deo.cmrefresher;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import android.widget.TextView;

public class MainActivity extends ListActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ArrayList<ContactItem> list = new ArrayList<ContactItem>();
        
        try {     
          setContentView(R.layout.main);
              
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