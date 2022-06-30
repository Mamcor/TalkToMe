package com.mcs.talktome.network;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {

    private static Retrofit sRetrofit = null;

    public static Retrofit getClient() {
        if (sRetrofit == null) {
            sRetrofit = new Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com/fcm/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }

        return sRetrofit;
    }
}
