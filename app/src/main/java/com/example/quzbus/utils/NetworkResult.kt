package com.example.quzbus.utils

sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null
) {

    class Success<T>(data: T) : NetworkResult<T>(data)
    class Error<T>(message: String?, data: T? = null) : NetworkResult<T>(data, message)
    class Loading<T> : NetworkResult<T>()

    /**
    fun result(networkResult: NetworkResult<T>): DataResult<T> {
        return when (networkResult) {
            is NetworkResult.Success<T> -> {
                if (networkResult.data != null) {
                    DataResult.Success(networkResult.data)
                }
                else {
                    DataResult.Error(message)
                }
            }
            is NetworkResult.Error<T> -> {
                DataResult.Error(networkResult.message)
            }
        }
    }
    */
}