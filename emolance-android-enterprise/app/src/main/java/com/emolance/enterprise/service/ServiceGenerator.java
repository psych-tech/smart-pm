package com.emolance.enterprise.service;

import android.util.Base64;

import com.emolance.enterprise.auth.ApiKeyProvider;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by yusun on 5/22/15.
 */
public class ServiceGenerator {

    // No need to instantiate this class.
    private ServiceGenerator() {

    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, final ApiKeyProvider apiKeyProvider) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create());
        builder.client(getOkHttpClientWithKeyProvider(apiKeyProvider.getAuthTokenValue()));
        return builder.build().create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, String username, String password) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create());
        builder.client(getSimpleOkHttpClient(username, password));
        return builder.build().create(serviceClass);
    }

    private static Interceptor getLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    private static OkHttpClient getOkHttpClientWithKeyProvider(final String key) {
        return new OkHttpClient.Builder()
                .addInterceptor(getLoggingInterceptor())
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        request = request.newBuilder()
                                .removeHeader("Authorization")
                                .addHeader("Authorization", key)
                                .addHeader("Acceppt", "application/json")
                                .build();

                        Response originalResponse = chain.proceed(request);
                        return originalResponse;
                    }
                })
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build();
    }

    private static OkHttpClient getSimpleOkHttpClient(String username, String password) {
        final String credentials = username + ":" + password;
        return new OkHttpClient.Builder()
                .addInterceptor(getLoggingInterceptor())
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        String token = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                        request = request.newBuilder()
                                .removeHeader("Authorization")
                                .addHeader("Authorization", token)
                                .addHeader("Acceppt", "application/json")
                                .build();

                        Response originalResponse = chain.proceed(request);
                        return originalResponse;
                    }
                })
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build();
    }

}
