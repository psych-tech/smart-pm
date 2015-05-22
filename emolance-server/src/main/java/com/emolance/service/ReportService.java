package com.emolance.service;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

import com.emolance.domain.ImageReport;


public interface ReportService {

	@GET("/process/{userId}")
	public void triggerProcess(@Path("userId")String userId, Callback<ImageReport> callback);

	@GET("/process/{userId}")
	public ImageReport triggerProcess(@Path("userId")String userId);

}
