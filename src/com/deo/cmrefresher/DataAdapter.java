package com.deo.cmrefresher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 *
 * @author deo
 */
public class DataAdapter extends ArrayAdapter<String>
{
    private String[] mContacts;
    
    Context mContext;
    
    DataAdapter(Context context, int resource, String[] data){
        super(context, resource, data);
        this.mContacts = data;
        this.mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent){
     TextView label = (TextView) convertView;
     if (convertView == null){
         convertView = new TextView(mContext);
         label = (TextView)  convertView;
     }
     label.setText(mContacts[position]);
     return (convertView);        
    }
    
    public String getItem(int position){
        return mContacts[position];
    }

}
    

