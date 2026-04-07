package com.example.nutrisnap.service;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface LogMealService {
    // Bỏ tiền tố "api/" vì domain đã là api.logmeal.es
    @Multipart
    @POST("v2/image/segmentation/complete")
    Call<ResponseBody> analyzeImage(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image
    );

    @POST("v2/recipe/nutritionalInfo")
    Call<ResponseBody> getNutritionalInfo(
            @Header("Authorization") String token,
            @Body RequestBody body
    );
}
