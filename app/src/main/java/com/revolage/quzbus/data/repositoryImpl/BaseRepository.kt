package com.revolage.quzbus.data.repositoryImpl

import com.revolage.quzbus.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

abstract class BaseRepository() {

    suspend fun <T> safeApiCall(apiToBeCalled: suspend () -> Response<T>): NetworkResult<T> {

        return withContext(Dispatchers.IO) {
            try {
                val response: Response<T> = apiToBeCalled()

                when {
                    response.isSuccessful -> {
                        NetworkResult.Success(data = response.body()!!)
                    }
                    response.code() == 400 -> {
                        NetworkResult.Error("Bad request")
                    }
                    response.code() == 401 -> {
                        NetworkResult.Error("Unauthorized")
                    }
                    response.code() == 403 -> {
                        NetworkResult.Error("Forbidden")
                    }
                    response.code() == 419 -> {
                        NetworkResult.Error("Refresh Token Limited")
                    }
                    response.code() == 500 -> {
                        NetworkResult.Error("Internal Server Error")
                    }
                    else -> {
                        NetworkResult.Error("Error")
                    }
                }
            } catch (e: HttpException) {
                NetworkResult.Error(e.message() ?: "Something went wrong")
            } catch (e: IOException) {
                NetworkResult.Error("Check your Network connection")
            } catch (e:Exception) {
                NetworkResult.Error(e.message)
            }
        }
    }
}