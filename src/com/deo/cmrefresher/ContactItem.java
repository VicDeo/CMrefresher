/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.deo.cmrefresher;

import java.util.HashMap;

/**
 *
 * @author deo
 */
public class ContactItem extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;
    public static final String TITLE = "title";
    public static final String DATE = "date";
    
    public ContactItem(String title, String date){
        super();
        super.put(TITLE, title);
        super.put(DATE, date);
    }
    
}