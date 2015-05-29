package com.emolance.app.service;

import com.emolance.app.data.Report;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by yusun on 5/22/15.
 */
public interface EmolanceAPI {

    @POST("/api/reports/trigger/process")
    public void triggerProcess(Callback<Response> callback);

    @GET("/api/reports")
    public void listReports(Callback<List<Report>> reports);

    @GET("/api/account")
    public Response authenticate();
}
