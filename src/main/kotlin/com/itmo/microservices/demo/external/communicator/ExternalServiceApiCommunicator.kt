package com.itmo.microservices.demo.external.communicator

import com.itmo.microservices.demo.OnlineShopDescriptor
import com.itmo.microservices.demo.common.exceptions.ExternalServiceException
import okhttp3.*
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.http.HttpStatus
import java.io.IOException
import java.net.URL
import java.time.Duration
import java.util.concurrent.ExecutorService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CachedResponseBody internal constructor(_body: ResponseBody) {
    private val string: String

    init {
        string = _body.string()
    }

    fun string() = string
}

class TrimmedResponse private constructor(
    private val body: CachedResponseBody,
    private val code: Int,
    private val req: Request
) {
    companion object {
        fun fromResponse(resp: Response): TrimmedResponse {
            return TrimmedResponse(CachedResponseBody(resp.body()!!), resp.code(), resp.request()).apply {
                resp.close()
            }
        }
    }

    fun body() = body
    fun code() = code
    fun request() = req
}

open class ExternalServiceApiCommunicator(
    private val descriptor: OnlineShopDescriptor,
    private val executor: ExecutorService
) {
    companion object {
        private val TIMEOUT = Duration.ofSeconds(20)
        private val JSON = MediaType.parse("application/json; charset=utf-8")
        fun JSONObject.toRequestBody() = RequestBody.create(JSON, this.toString())
    }

    private val client = OkHttpClient.Builder().run {
        dispatcher(Dispatcher(executor))
        callTimeout(TIMEOUT)
        build()
    }

    open suspend fun createProject(name: String): CreateProjectResponse? =
        execute("createProject", "/management/projects") {
            jsonPost(
                "name" to name,
            )

        }.run {
            val resp = body().string()
            mapper.readValue(resp, CreateProjectResponse::class.java)
        }

    suspend fun execute(method: String, url: String) = execute(method, url) {}

    suspend fun execute(method: String, url: String, builderContext: CustomRequestBuilder.() -> Unit): TrimmedResponse {
        val requestBuilder = CustomRequestBuilder(descriptor.url).apply {
            _url(url)
            builderContext(this)
        }
        return suspendCoroutine {
            val req = requestBuilder.build()

            client.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (HttpStatus.Series.resolve(response.code()) == HttpStatus.Series.SUCCESSFUL) {
                        try {
                            it.resume(TrimmedResponse.fromResponse(response))
                        } catch (t: Throwable) {
                            it.resumeWithException(t)
                        }
                        return
                    }
                    it.resumeWithException(
                        ExternalServiceException(
                            "${response.request().method()} ${
                                response.request().url()
                            } External service returned non-OK code: ${response.code()}\n\n${response.body()?.string()}"
                        )
                    )
                }
            })
        }
    }

    class CustomRequestBuilder(private val baseUrl: URL) : Request.Builder() {
        companion object {
            val emptyBody = RequestBody.create(null, ByteArray(0))
        }

        fun _url(url: String) = super.url(URL(baseUrl, url))

        /**
         * Don't call this method in builder
         */
        @Deprecated("Not allowed to use")
        override fun url(url: URL) = throw IllegalStateException("Not allowed to call this function")

        /**
         * Don't call this method in builder
         */
        @Deprecated("Not allowed to use")
        override fun url(url: String) = throw IllegalStateException("Not allowed to call this function")

        fun jsonPost(ctx: JSONObject.() -> Unit) {
            val obj = JSONObject()
            ctx(obj)
            post(obj.toRequestBody())
        }

        fun jsonPost(vararg items: Pair<String, String>) {
            post(JSONObject().withItems(*items).toRequestBody())
        }

        fun post() {
            post(emptyBody)
        }

        fun put() {
            put(emptyBody)
        }
    }
}