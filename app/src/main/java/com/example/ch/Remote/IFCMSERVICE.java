package com.example.ch.Remote;

import com.example.ch.Models.FCMResponse;
import com.example.ch.Models.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMSERVICE {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=APA91bEEukl0hOK0UZSFlIuRRsuDGyJeDBQAHdwsAAudea9wADMRI6q5asDM249DYVEuBq3-kSUH0ABTK80Xt1Z4UNTAzQyLtQsXt9jTkanV2bwExWgyUptqGxzrvJZg8GdoTnleS4vX"
    })

    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
