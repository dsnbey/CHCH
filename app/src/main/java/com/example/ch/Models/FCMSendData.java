package com.example.ch.Models;

import java.util.Map;

public class FCMSendData {
    private String to;
    private Map<String, String> data;

    public FCMSendData() {
    }

    public FCMSendData(String to, Map<String, String> data) {
        this.data = data;
        this.to = to;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
