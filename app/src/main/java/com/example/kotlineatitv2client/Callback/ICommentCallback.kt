package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.CommentModel

interface ICommentCallback {
    fun onCommentLoadSuccess(commentList:List<CommentModel>)
    fun onCommentLoadFailed(message:String)
}