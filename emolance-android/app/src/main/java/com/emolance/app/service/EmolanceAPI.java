package com.emolance.app.service;

import com.emolance.app.data.Report;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by yusun on 5/22/15.
 */
public interface EmolanceAPI {

    @POST("/api/reports/trigger/process")
    public void triggerProcess(Callback<Response> callback);

    @GET("/api/reports")
    public void listReports(Callback<List<Report>> reports);

    @GET("/api/reports/{id}")
    public void getReport(@Path("id") Long id, Callback<Report> report);

    @GET("/api/account")
    public Response authenticate();

    @POST("/api/reports/device/trigger/process/{sn}")
    public void triggerProcess(
            @Path("sn") String sn,
            @Query("qrcode") String qrcode,
            @Query("delay") Integer delay,
            Callback<Response> callback);

    @POST("/api/reports/user/create/{qrcode}")
    public void createUserReport(
            @Path("qrcode") String qrcode,
            @Query("name") String name,
            @Query("link") String link,
            @Query("age") String age,
            @Query("position") String position,
            Callback<Response> callback);


}
