package com.emolance.enterprise.service;

import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.Report;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * Created by yusun on 5/22/15.
 */
public interface EmolanceAPI {

    @POST("/api/reports/trigger/process")
    public Call<ResponseBody> triggerProcess();

    @GET("/api/my-users")
    public Call<List<EmoUser>> listMyUsers();

    @GET("/api/reports")
    public Call<List<Report>> listReports();

    @GET("/api/reports/{id}")
    public Call<Report> getReport(@Path("id") Long id);

    @GET("/api/account")
    public Call<ResponseBody> authenticate();

    @POST("/api/reports/device/trigger/process/{sn}")
    public Call<ResponseBody> triggerProcess(
            @Path("sn") String sn,
            @Query("qrcode") String qrcode,
            @Query("delay") Integer delay);

    @POST("/api/reports/user/create/{qrcode}")
    public Call<ResponseBody> createUserReport(
            @Path("qrcode") String qrcode,
            @Query("name") String name,
            @Query("link") String link,
            @Query("age") String age,
            @Query("position") String position);

}
