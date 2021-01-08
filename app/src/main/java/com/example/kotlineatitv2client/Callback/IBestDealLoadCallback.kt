package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.BestDealModel

interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealList:List<BestDealModel>)
    fun onBestDealLoadFailed(message:String)
}