package com.basit.aitattoomaker.data.repo

import androidx.annotation.Keep



@Keep
data class ResponsesKv(
    val `data`: List<Data>,
    val errors: Errors,
    val message: String,
    val status: Int
)

@Keep
data class Data(
    val key: String,
    val value: String
)
@Keep
data class MainErrorResponse(
    val status: String,
    val message: String?,
    val errors: Errors
)

@Keep
data class Errors(
    val field_errors: FieldErrors,
    val non_field_errors: List<String>?
)
@Keep
data class FieldErrors(
    //for models and styles error
    val limit: List<String>?,
    val offset: List<String>?,
    val ordering: List<String>?,
    val category: List<String>?,
    val bookmarked: List<String>?,
    val featured: List<String>?,

    // createImageWthNewWay error
    val prompt: List<String>?,
    val model: List<String>?,
    val template: List<String>?,
    val aspect_ratio: List<String>?,
    val samples: List<String>?,
    val seed: List<String>?,
    val guidance_scale: List<String>?,
    val steps: List<String>?,

    // signup error
    val email: List<String>?,
    val first_name: List<String>?,
    val last_name: List<String>?,
    val password: List<String>?,


    // verifyOtp error
    val code:List<String>?,
    val context:List<String>?,
    val string:List<String>?,

    // postImageWthNewWay error
    val title: List<String>?,
    val short_description: List<String>?,
    val images : List<String>?,
    val hash: List<String>?,


    //reportUser
    val reason: List<String>?,
    val description: List<String>?,



    //exposePrompt
    val postId: List<String>?,

    //update profile
    val avatar: List<String>?,
    val cover: List<String>?,
    val profileId: List<String>?,


    val image_url: List<String>?,
    val feedback_status: List<String>?,

    val user_id: List<String>?,
    val id: List<String>?,
    val authToken: String?,


    )