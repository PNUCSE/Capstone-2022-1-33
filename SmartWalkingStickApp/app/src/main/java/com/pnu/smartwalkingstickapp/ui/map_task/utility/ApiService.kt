package com.pnu.smartwalkingstickapp.ui.map_task.utility

import com.pnu.smartwalkingstickapp.ui.map_task.response.path.FeatureCollection
import com.pnu.smartwalkingstickapp.ui.map_task.response.search.SearchResponse
import com.pnu.smartwalkingstickapp.ui.map_task.utility.Key
import com.pnu.smartwalkingstickapp.ui.map_task.utility.Url
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {

    @GET(Url.GET_TMAP_LOCATION)
    suspend fun getSearchLocation(
        @Header("appKey") appKey: String = Key.TMAP_API,
        @Query("version") version: Int = 1,
        @Query("callback") callback: String? = null,
        @Query("count") count: Int = 20,
        @Query("searchKeyword") keyword: String,
        @Query("areaLLCode") areaLLCode: String? = null,
        @Query("areaLMCode") areaLMCode: String? = null,
        @Query("resCoordType") resCoordType: String? = null,
        @Query("searchType") searchType: String? = null,
        @Query("multiPoint") multiPoint: String? = null,
        @Query("searchtypCd") searchtypCd: String? = null,
        @Query("radius") radius: String? = null,
        @Query("reqCoordType") reqCoordType: String? = null,
        @Query("centerLon") centerLon: String? = null,
        @Query("centerLat") centerLat: String? = null
    ): Response<SearchResponse>

    @GET(Url.GET_TMAP_PATH_URL)
    suspend fun getPath(
        @Header("appKey") appKey: String = Key.TMAP_API,
        @Query("version") version: Int = 1,
        @Query("callback") callback: String? = null,

        @Query("startX") startX: Float,
        @Query("startY") startY: Float,
        @Query("endX") endX: Float,
        @Query("endY") endY: Float,
        @Query("startName") startName: String,
        @Query("endName") endName: String,
    ): Response<FeatureCollection>

 /*   @GET(Url.GET_TMAP_REVERSE_GEO_CODE)
    suspend fun getReverseGeoCode(
        @Header("appKey") appKey: String = Key.TMAP_API,
        @Query("version") version: Int = 1,
        @Query("callback") callback: String? = null,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("coordType") coordType: String? = null,
        @Query("addressType") addressType: String? = null
    ): Response<AddressInfoResponse>*/
}
