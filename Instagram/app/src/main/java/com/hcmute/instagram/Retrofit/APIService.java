package com.hcmute.instagram.Retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {
    @FormUrlEncoded
    @POST("predict_toxic_content")
    Call<Boolean> postPredictContent(@Field("content") String content);
}
