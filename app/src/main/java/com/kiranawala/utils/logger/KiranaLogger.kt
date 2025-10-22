package com.kiranawala.utils.logger

import timber.log.Timber

object KiranaLogger {
    fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, message)
        } else {
            Timber.tag(tag).e(message)
        }
    }
    
    fun w(tag: String, message: String) {
        Timber.tag(tag).w(message)
    }
    
    fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }
}