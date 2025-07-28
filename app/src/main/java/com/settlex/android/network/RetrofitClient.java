package com.settlex.android.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    /*-------------------------------------
    Base URL for Firebase HTTPS function
    -------------------------------------*/
    private static final String BASE_URL = "https://backendapi-v5ij3be3ca-nw.a.run.app/";
    private static Retrofit retrofit = null;

    /*----------------------------------------------
    Returns singleton instance of Retrofit service
    ----------------------------------------------*/
    public static FirebaseFunctionService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(FirebaseFunctionService.class);
    }
}
