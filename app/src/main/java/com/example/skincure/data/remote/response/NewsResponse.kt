package com.example.skincure.data.remote.response

import com.google.gson.annotations.SerializedName

typealias NewsResponse = List<NewsResponseItem>

data class NewsResponseItem(

	@field:SerializedName("imageUrl")
	val image: String,

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("id")
	val id: String
)