package com.vanoprojects.voxera.data.api

import com.vanoprojects.voxera.data.model.AnalysisResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VoxeraApi {
    @Multipart
    @POST("integrations/analyze")
    suspend fun analyze(
        @Part audio: MultipartBody.Part,
        @Part("analysis_type") analysisType: RequestBody
    ): Response<AnalysisResponse>
}
