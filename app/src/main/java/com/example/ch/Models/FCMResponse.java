package com.example.ch.Models;

import java.util.List;

public class FCMResponse {
    private long multicastID, messageID ;
    private int success, failure, canonical_ids;
    private List<FCMResult> results;

    public FCMResponse() {
    }

    public long getMulticastID() {
        return multicastID;
    }

    public int getFailure() {
        return failure;
    }

    public List<FCMResult> getResults() {
        return results;
    }

    public void setResults(List<FCMResult> results) {
        this.results = results;
    }

    public int getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(int canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public void setMulticastID(long multicastID) {
        this.multicastID = multicastID;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }
}

