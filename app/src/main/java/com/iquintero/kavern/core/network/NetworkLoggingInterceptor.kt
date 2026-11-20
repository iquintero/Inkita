package com.iquintero.kavern.core.network

import com.iquintero.kavern.core.logging.LoggingManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Lightweight HTTP interceptor that logs request path/status/duration when verbose logging is enabled.
 * Does not log bodies to avoid leaking sensitive data.
 */
object NetworkLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val start = System.currentTimeMillis()
        return try {
            val response = chain.proceed(request)
            if (LoggingManager.isDebugEnabled()) {
                val took = System.currentTimeMillis() - start
                LoggingManager.d(
                    "HTTP",
                    "${request.method} ${request.url.encodedPath} -> ${response.code} (${took}ms)",
                )
            }
            response
        } catch (e: Exception) {
            if (LoggingManager.isDebugEnabled()) {
                LoggingManager.e(
                    "HTTP",
                    "${request.method} ${request.url.encodedPath} failed: ${e.javaClass.simpleName}: ${e.message}",
                    e,
                )
            }
            throw e
        }
    }
}
