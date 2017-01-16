package com.emolance.enterprise.service;

import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.TestReport;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @GET("/api/emo/reports/byOwner/{id}")
    public Call<List<TestReport>> listReports(@Path("id") Long ownerId);

    @GET("/api/test-reports/{id}")
    public Call<TestReport> getReport(@Path("id") Long id);

    @GET("/api/account")
    public Call<ResponseBody> authenticate();

    @POST("/api/reports/device/trigger/process/{sn}")
    public Call<ResponseBody> triggerProcess(
            @Path("sn") String sn,
            @Query("qrcode") String qrcode,
            @Query("delay") Integer delay);

    @POST("/api/test-reports")
    public Call<ResponseBody> createUserReport(@Body TestReport testReport);

    @PUT("/api/test-reports")
    public Call<ResponseBody> updateUserReport(@Body TestReport testReport);

}
