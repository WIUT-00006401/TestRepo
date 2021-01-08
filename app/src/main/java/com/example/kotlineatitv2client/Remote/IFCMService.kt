package com.example.kotlineatitv2client.Remote

import com.example.kotlineatitv2client.Model.FCMResponse
import com.example.kotlineatitv2client.Model.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAY-_OwV4:APA91bFxUxre61kPifIgLBJj3L5q1_3ud2WCA4-jbQrRzGwGELlEZVnIq1uvt6I2RDTyutNnLT-_tErdmO_tL7RHhIudwVmVZwuy-wzOBpYdYi1ZJF9V2hFt8rOOUMNglgAnaXP3wub0"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData): Observable<FCMResponse>
}