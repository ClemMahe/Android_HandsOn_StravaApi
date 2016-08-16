package com.clem.jstravawrapper.network;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JStravaV3Retrofit{


    private String accessToken;
    private JStravaRetrofit mServiceStrava;


    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Constructor
     * @param access_token Access token Strava
     */
    public JStravaV3Retrofit(String access_token){

        this.accessToken = access_token;

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addNetworkInterceptor(logging);

        httpClient.addInterceptor(new Interceptor() {
                                      @Override
                                      public Response intercept(Interceptor.Chain chain) throws IOException {
                                          Request original = chain.request();
                                          Request request = original.newBuilder()
                                                  .header("Accept", "application/json")
                                                  .header("Authorization", "Bearer "+getAccessToken())
                                                  .method(original.method(), original.body())
                                                  .build();
                                          return chain.proceed(request);
                                      }
                                  });
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.strava.com/api/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        mServiceStrava = retrofit.create(JStravaRetrofit.class);

    }

    public JStravaRetrofit getService(){
        return mServiceStrava;
    }




}
