package com.vanoprojects.voxera.data.model

import com.google.gson.annotations.SerializedName

data class AnalysisResponse(
    val success: Boolean = false,
    @SerializedName("analysis_type") val analysisType: String = "",
    val result: AnalysisResult? = null
)

data class AnalysisResult(
    @SerializedName("psy_types") val psyTypes: List<PsyType>? = null,
    @SerializedName("emo_scales") val emoScales: List<EmoScale>? = null,
    @SerializedName("description")
    val description: String? = null
)

data class PsyType(
    val id: Int,
    val name: String,
    val value: Double
)

data class EmoScale(
    val id: Int,
    val name: String,
    val value: Int
)
