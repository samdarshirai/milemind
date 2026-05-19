package com.company.runcoach.feature.auth.data

import com.company.runcoach.feature.auth.domain.AuthFailure
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthErrorMapperTest {

    private val mapper = AuthErrorMapper()

    @Test
    fun mapsValidationErrorFromBackendFormat() {
        val response = Response.error<Any>(
            400,
            """
                {"error":{"code":"VALIDATION_ERROR","message":"Invalid","details":[{"field":"email","issue":"invalid"}]}}
            """.trimIndent().toResponseBody("application/json".toMediaType())
        )
        val result = mapper.map(HttpException(response))

        assertTrue(result is AuthFailure.Validation)
        assertEquals("Invalid", result.message)
    }

    @Test
    fun mapsConnectivity() {
        val result = mapper.map(IOException("offline"))
        assertTrue(result is AuthFailure.Connectivity)
    }
}
