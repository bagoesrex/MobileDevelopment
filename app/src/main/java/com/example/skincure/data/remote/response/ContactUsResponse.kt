package com.example.skincure.data.remote.response

import com.google.gson.annotations.SerializedName

data class ContactUsResponse(

	@field:SerializedName("message")
	val message: String
)
