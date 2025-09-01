package com.basit.aitattoomaker.data.repo

import com.google.gson.Gson


var errorResponseList: List<Data>? = null


fun getErrorClassObject(errorBody : String): MainErrorResponse {
    val gson = Gson()
    val errorResponse = gson.fromJson(errorBody, MainErrorResponse::class.java)
    return errorResponse
}