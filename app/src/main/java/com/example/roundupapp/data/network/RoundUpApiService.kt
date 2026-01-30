package com.example.roundupapp.data.network

import com.example.roundupapp.data.network.models.NetworkAccountsWrapper
import com.example.roundupapp.utils.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

private val retrofit = Retrofit.Builder()
  .addConverterFactory(GsonConverterFactory.create())
  .baseUrl(BASE_URL)
  .build()

interface RoundUpApiService {

  @GET("accounts")
  suspend fun getAccounts(@Header("Authorization") accessToken: String): NetworkAccountsWrapper
}

object RoundUpApi {
  val retrofitService: RoundUpApiService by lazy {
    retrofit.create(RoundUpApiService::class.java)
  }
}
