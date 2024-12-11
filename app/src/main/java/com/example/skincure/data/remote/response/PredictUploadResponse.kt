package com.example.skincure.data.remote.response

import com.google.gson.annotations.SerializedName

data class PredictUploadResponse(

	@field:SerializedName("confidence_score")
	val score: Double,

	@field:SerializedName("result")
	val result: String,

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("status_code")
	val statusCode: Int,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("id")
	val id: String
)
