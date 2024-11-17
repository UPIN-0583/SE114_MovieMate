package com.example.moviemate.api;

import com.example.moviemate.api.objects.payos.CreatePayLink;
import com.example.moviemate.api.objects.payos.CreatePayLinkResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface PayOsApiService {
    Gson gson = new GsonBuilder().setDateFormat("dd/mm/yyyy").create();

    PayOsApiService payOsApiService = new Retrofit.Builder()
            .baseUrl("https://api-merchant.payos.vn/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PayOsApiService.class);

    @POST("v2/payment-requests/")
    Call<CreatePayLinkResponse> createPayLink(
            @Header("x-client-id") String clientId,
            @Header("x-api-key") String apiKey,
            @Body CreatePayLink createPayLink);
}
