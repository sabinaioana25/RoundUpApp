package com.example.roundupapp.data.network

import com.example.roundupapp.data.network.models.account.NetworkAccountsWrapper
import com.example.roundupapp.data.network.models.balance.NetworkBalance
import com.example.roundupapp.data.network.models.savingsgoals.CreateAmountTransferRequest
import com.example.roundupapp.data.network.models.savingsgoals.CreateAmountTransferResponse
import com.example.roundupapp.data.network.models.transactions.NetworkTransactionsWrapper
import com.example.roundupapp.data.network.models.savingsgoals.CreateSavingsGoalRequest
import com.example.roundupapp.data.network.models.savingsgoals.CreateSavingsGoalResponse
import com.example.roundupapp.data.network.models.savingsgoals.NetworkSavingsGoalsWrapper
import com.google.gson.GsonBuilder
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

const val BASE_URL = "https://api-sandbox.starlingbank.com/api/v2/"
const val QUERY_HEADER = "Authorization"
const val QUERY_PATH_ACCOUNT = "accountUid"
const val QUERY_PATH_CATEGORY = "categoryUid"
const val QUERY_CHANGES_SINCE = "changesSince"
const val QUERY_SAVINGS_GOAL_UID = "savingsGoalUid"
const val QUERY_PATH_TRANSFER_UID = "transferUid"

private val gson = GsonBuilder()
  .create()

private val retrofit = Retrofit.Builder()
  .addConverterFactory(GsonConverterFactory.create(gson))
  .baseUrl(BASE_URL)
  .build()

interface RoundUpApiService {

  @GET("accounts")
  suspend fun getAccounts(
    @Header(QUERY_HEADER) accessToken: String,
  ): NetworkAccountsWrapper

  @GET("accounts/{accountUid}/balance")
  suspend fun getBalance(
    @Header(QUERY_HEADER) accessToken: String,
    @Path(QUERY_PATH_ACCOUNT) accountUid: String,
  ): NetworkBalance

  @GET("feed/account/{accountUid}/category/{categoryUid}")
  suspend fun getTransactions(
    @Header(QUERY_HEADER) accessToken: String,
    @Path(QUERY_PATH_ACCOUNT) accountUid: String,
    @Path(QUERY_PATH_CATEGORY) categoryUid: String,
    @Query(QUERY_CHANGES_SINCE) changesSince: String,
  ): NetworkTransactionsWrapper

  @GET("account/{accountUid}/savings-goals")
  suspend fun getSavingsGoals(
    @Header(QUERY_HEADER) accessToken: String,
    @Path(QUERY_PATH_ACCOUNT) accountUid: String,
  ): NetworkSavingsGoalsWrapper

  @PUT("account/{accountUid}/savings-goals")
  suspend fun createSavingsGoal(
    @Header(QUERY_HEADER) accessToken: String,
    @Path(QUERY_PATH_ACCOUNT) accountUid: String,
    @Body body: CreateSavingsGoalRequest,
  ): CreateSavingsGoalResponse

  @DELETE("account/{accountUid}/savings-goals/{savingsGoalUid}")
  suspend fun deleteSavingsGoal(
    @Header(QUERY_HEADER) accessToken: String,
    @Path(QUERY_PATH_ACCOUNT) accountUid: String,
    @Path(QUERY_SAVINGS_GOAL_UID) savingsGoalUid: String,
  ): Response<Unit>

  @PUT("account/{accountUid}/savings-goals/{savingsGoalUid}/add-money/{transferUid}")
  suspend fun transferMoneyToSavingsGoal(
    @Header(QUERY_HEADER) accessToken: String,
    @Path(QUERY_PATH_ACCOUNT) accountUid: String,
    @Path(QUERY_SAVINGS_GOAL_UID) savingsGoalUid: String,
    @Path(QUERY_PATH_TRANSFER_UID) transferUid: String,
    @Body body: CreateAmountTransferRequest
  ): CreateAmountTransferResponse
}

object RoundUpApi {
  val retrofitService: RoundUpApiService by lazy {
    retrofit.create(RoundUpApiService::class.java)
  }
}
