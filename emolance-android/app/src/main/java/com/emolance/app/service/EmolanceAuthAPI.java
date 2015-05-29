package com.emolance.app.service;

import retrofit.client.Response;
import retrofit.http.GET;

/**
 * Created by yusun on 5/22/15.
 */
public interface EmolanceAuthAPI {

    @GET("/api/account")
    public Response authenticate();
}
