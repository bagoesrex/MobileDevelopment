package com.example.skincure.data.remote.response

import com.google.gson.annotations.SerializedName

data class PredictHistoriesResponse(

	@field:SerializedName("histories")
	val histories: List<HistoriesItem>
)

data class HistoriesItem(

	@field:SerializedName("result")
	val result: String,

	@field:SerializedName("uid")
	val uid: String,

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("status_code")
	val statusCode: Int,

	@field:SerializedName("image_url")
	val imageUrl: String,

	@field:SerializedName("confidence_score")
	val confidenceScore: String,

	@field:SerializedName("description")
	val description: String
)
