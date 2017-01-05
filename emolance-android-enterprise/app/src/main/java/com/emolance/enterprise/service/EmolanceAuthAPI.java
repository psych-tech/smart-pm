package com.emolance.enterprise.service;

import com.emolance.enterprise.data.EmoUser;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by yusun on 5/22/15.
 */
public interface EmolanceAuthAPI {

    @GET("/api/emo/current-emo")
    public Call<EmoUser> authenticate();

}
