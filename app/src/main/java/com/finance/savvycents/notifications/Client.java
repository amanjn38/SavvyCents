package com.finance.savvycents.notifications;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {

    public static Retrofit retrofit = null;

    public static Retrofit getRetrofit(String uri) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(uri).
                    addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
