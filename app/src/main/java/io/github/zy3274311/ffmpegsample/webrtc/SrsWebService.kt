package io.github.zy3274311.ffmpegsample.webrtc

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SrsWebService {
    @POST("/rtc/v1/publish/")
    fun requestPublish(@Body body: SrsPublishRequestBody):Call<SrsPublishResponseBody>
}