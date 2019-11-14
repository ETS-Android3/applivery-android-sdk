/*
 * Copyright (c) 2019 Applivery
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.applivery.applvsdklib.network.api.model

import com.applivery.applvsdklib.domain.model.ErrorObject
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException


data class ErrorDataEntityResponse(
    val error: ErrorEntity,
    val status: Boolean
) {

    companion object {

        fun parseError(response: Response<*>): ErrorEntity {

            val gson = Gson()

            return try {
                val errorResponse =
                    gson.fromJson(
                        response.errorBody()?.string(),
                        ErrorDataEntityResponse::class.java
                    )

                errorResponse?.error ?: ErrorEntity(
                    0, "Null error response",
                    null, false
                )
            } catch (e: IOException) {
                ErrorEntity(
                    0, "Parse error exception: ${e.message}",
                    null, false
                )
            }
        }
    }
}

data class ErrorEntity(
    val code: Int,
    val message: String,
    val data: ErrorDataEntity?,
    val businessCode: Boolean = true
) {

    fun toErrorObject() = ErrorObject(
        businessCode,
        message,
        code
    )
}

data class ErrorDataEntity(
    val providers: List<String>
)
