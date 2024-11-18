package com.example.whatsappclone.data


open class Events<out T>(val content : T) {
    var hasHandled = false
    fun getContentOrNull() : T?{
        return if(hasHandled) null
        else{
            hasHandled = true
            content
        }
    }

}
