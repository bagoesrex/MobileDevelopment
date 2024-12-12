package com.example.skincure.data.remote.response

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class PredictHistoriesResponse(

	@field:SerializedName("histories")
	val histories: List<HistoriesItem>,

	val count: Int
)

@Entity(tableName = "histories")
data class HistoriesItem(

	@field:SerializedName("result")
	val result: String,

	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,

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
