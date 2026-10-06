package BugBug.androidApp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GoldApiService {

    @GET("scripts/xml_metall.asp")
    suspend fun getGoldRate(
        @Query("date_req1") date1: String,
        @Query("date_req2") date2: String
    ): GoldResponse
} // полный путь будет чето https://www.cbr.ru/scripts/xml_metall.asp?date_req1=01/10/2026&date_req2=07/10/2026