package com.deo.cmrefresher;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;

public class Message implements Comparable<Message> {

    static SimpleDateFormat INPUTFORMATTER =
            new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    static SimpleDateFormat OUTPUTFORMATTER =
            new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
    private String title;
    private URL link;
    private String description ="";
    private Date date;
    private String adate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    public URL getLink() {
        return link;
    }

    public void setLink(String link) {
        try {
            this.link = new URL(
                    "http://download.cyanogenmod.com/get/"+link.split("#")[1]);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
    
    public String getDate() {
        return OUTPUTFORMATTER.format(this.date);
    }

    public Date getDateObj() {
        return date;
    }
    
    public void setDate(String date) {
        // pad the date if necessary
        while (!date.endsWith("00")) {
            date += "0";
        }
        date = date.substring(4, date.length());
        try {
            this.date = INPUTFORMATTER.parse(date.trim());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Message copy() {
        Message copy = new Message();
        copy.title = title;
        copy.link = link;
        copy.description = description;
        copy.date = date;
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        sb.append("\n");
        sb.append(this.getDate());
        return sb.toString();
    }

    public HashMap<String, String> toHashMap() {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("title", title + " baked on " + this.getDate());
        return hash;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((link == null) ? 0 : link.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Message other = (Message) obj;
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (link == null) {
            if (other.link != null) {
                return false;
            }
        } else if (!link.equals(other.link)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        return true;
    }

    public int compareTo(Message another) {
        if (another == null) {
            return 1;
        }
        // sort descending, most recent first
        return another.date.compareTo(date);
    }
}