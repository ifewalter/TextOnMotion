package com.ifewalter.android.textonmotion.objects;

import java.util.ArrayList;

/**
 * Created by iFewalter on 10/20/13.
 */
public class SMSObject {

    private ArrayList<String> number;
    private String from;
    private String message;

    public SMSObject()
    {}

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public ArrayList<String> getTo() {
        return number;
    }

    public void setTo(ArrayList<String> to) {
        this.number = to;
    }
}
