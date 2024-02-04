package com.finance.savvycents.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAPyywzas:APA91bFiGHtGGz6O4sj95TfpN1MnVUu0pamL8EDuwFAxRzEUmfIlhl4tQKT4h7Zgc9gPFtFTWNOxRP082SCaRrHja_vKGg1s3RMVhV06b7ZrmymdwIexcGT_YYidED2ga65yDv7noCcP"

    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
