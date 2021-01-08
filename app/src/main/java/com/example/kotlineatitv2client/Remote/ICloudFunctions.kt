package com.example.kotlineatitv2client.Remote

import com.example.kotlineatitv2client.Model.BraintreeToken
import com.example.kotlineatitv2client.Model.BraintreeTransaction
import io.reactivex.Observable
import retrofit2.http.*


interface ICloudFunctions {
    @GET("token")
    fun getToken(@HeaderMap headers: Map<String,String>): Observable<BraintreeToken>

    @POST("checkout")
    @FormUrlEncoded
    fun submitPayment(
        @HeaderMap headers: Map<String,String>,
        @Field("amount")amount:Double,
                      @Field("payment_method_nonce")nonce:String):Observable<BraintreeTransaction>
}