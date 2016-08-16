package com.clem.jstravawrapper.network;

import org.jstrava.entities.activity.UploadStatus;
import org.jstrava.entities.athlete.Athlete;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by roberto on 12/26/13.
 */
public interface JStravaRetrofit {


    @GET("athlete")
    public Call<Athlete> getCurrentAthlete();


    @Headers({
            "Connection: Keep-Alive",
            "Cache-Control: no-cache",
            "data_type: gpx"
    })
    @Multipart
    @POST("uploads")
    public Call<UploadStatus> uploadActivity(
            @Part MultipartBody.Part file, @Part("data_type") RequestBody dataType);


}
