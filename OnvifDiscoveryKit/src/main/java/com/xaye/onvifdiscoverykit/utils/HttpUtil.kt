package com.xaye.onvifdiscoverykit.utils

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.util.UUID
import java.util.regex.Pattern

/**
 * Author xaye
 * @date: 2024/12/24
 */
object HttpUtil {

    /**
     * POST 请求
     */
    @Throws(Exception::class)
    fun postRequest(baseUrl: String?, params: String): String {
        val url = URL(baseUrl)
        with(url.openConnection() as HttpURLConnection) {
            // 设置请求属性
            requestMethod = "POST"
            doInput = true
            doOutput = true
            useCaches = false
            instanceFollowRedirects = true
            setRequestProperty("Content-Type", "application/soap+xml")

            // 发送请求数据
            outputStream.write(params.toByteArray())

            // 判断请求是否成功
            if (responseCode == 200) {
                return inputStream.bufferedReader().use { it.readText() }
            } else {
                throw Exception("ResponseCodeError: $responseCode")
            }
        }
    }

    /**
     * 下载图片(GET的请求方式)
     *
     * @param webSite url地址
     * @return byte[] 图片的字节数据
     */
    fun getByteArray(webSite: String): ByteArray? {
        var inputStream: InputStream? = null
        var byteArrayOutputStream: ByteArrayOutputStream? = null

        return try {
            val url = URL(webSite)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                readTimeout = 8000
            }

            if (connection.responseCode == 200) {
                inputStream = connection.inputStream
                byteArrayOutputStream = ByteArrayOutputStream()

                inputStream.use { input ->
                    byteArrayOutputStream.use { output ->
                        val buffer = ByteArray(1024 * 4)
                        var len: Int
                        while (input.read(buffer).also { len = it } != -1) {
                            output.write(buffer, 0, len)
                        }
                    }
                }
                byteArrayOutputStream.toByteArray()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            // No need to manually close streams as `use` takes care of that
        }
    }

    /**
     * 下载图片并处理Digest鉴权请求
     *
     * @param url 请求的URL
     * @param username 用户名
     * @param password 密码
     * @return 图片的字节数组或null
     */
    fun getImageWithDigestAuth(url: String, username: String, password: String): ByteArray? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        return when {
            response.isSuccessful && response.body() != null -> {
                response.body()!!.bytes()  // 如果请求成功，返回图片的字节数据
            }
            response.code() == 401 -> {
                // 如果是未认证，进行Digest认证
                val authHeaders = response.headers().values("WWW-Authenticate")
                val (qop, realm, nonce) = parseAuthHeaders(authHeaders)

                val method = request.method()
                val uriPath = url.substringAfter(response.request().url().host())

                digestHttp(url, username, password, method, uriPath, nonce, realm, qop)
            }
            else -> null  // 其他情况返回null
        }
    }

    /**
     * 解析WWW-Authenticate头部中的qop, realm, nonce
     *
     * @param authHeaders WWW-Authenticate头部列表
     * @return Triple(qop, realm, nonce)
     */
    private fun parseAuthHeaders(authHeaders: List<String>): Triple<String, String, String> {
        val qopPattern = Pattern.compile("qop=\"(.*?)\"")
        val realmPattern = Pattern.compile("realm=\"(.*?)\"")
        val noncePattern = Pattern.compile("nonce=\"(.*?)\"")

        var qop = ""
        var realm = ""
        var nonce = ""

        authHeaders.forEach { header ->
            qopPattern.matcher(header).takeIf { it.find() }?.let {
                qop = it.group(1)
            }
            realmPattern.matcher(header).takeIf { it.find() }?.let {
                realm = it.group(1)
            }
            noncePattern.matcher(header).takeIf { it.find() }?.let {
                nonce = it.group(1)
            }
        }

        return Triple(qop, realm, nonce)
    }

    /**
     * HTTP鉴权
     * @param url 请求的URL
     * @param user 用户名
     * @param psd 密码
     * @param method 请求方法
     * @param disgestUriPath URI路径
     * @param nonce 服务器返回的nonce
     * @param realm 认证领域
     * @param qop 保护质量
     * @return 响应字节数组
     * @throws IOException
     */
    @Throws(IOException::class)
    fun digestHttp(
        url: String, user: String, psd: String, method: String, disgestUriPath: String,
        nonce: String, realm: String, qop: String
    ): ByteArray? {
        val nc = "00000001" // 请求计数
        val cnonce = generateNonce()
        val ha1 = MD5Util.MD5Encode(createMd5Data(user, realm, psd))
        val ha2 = MD5Util.MD5Encode(createMd5Data(method, disgestUriPath))
        val ha3 = MD5Util.MD5Encode(createMd5Data(ha1, nonce, nc, cnonce, qop, ha2))
        val responseData = ha3

        val authorizationHeader = buildAuthorizationHeader(user, realm, nonce, disgestUriPath, cnonce, nc, responseData, qop)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", authorizationHeader)
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.bytes()
        }
        return null
    }

    /**
     * 生成用于MD5计算的参数数据
     * @param params 参数
     * @return 合并后的字符串
     */
    private fun createMd5Data(vararg params: String): String {
        return params.joinToString(":")
    }

    /**
     * 构建Authorization头部
     * @param user 用户名
     * @param realm 认证领域
     * @param nonce 服务器返回的nonce
     * @param uri URI路径
     * @param cnonce 客户端nonce
     * @param nc 请求计数
     * @param responseData 响应数据
     * @param qop 保护质量
     * @return 构建后的Authorization头部字符串
     */
    private fun buildAuthorizationHeader(
        user: String, realm: String, nonce: String, uri: String, cnonce: String,
        nc: String, responseData: String, qop: String
    ): String {
        return "Digest username=\"$user\",realm=\"$realm\",nonce=\"$nonce\",uri=\"$uri\",cnonce=\"$cnonce\",nc=$nc,response=\"$responseData\",qop=\"$qop\""
    }

    /**
     * 生成客户端nonce
     * @return 随机生成的nonce字符串
     */
    private fun generateNonce(): String {
        val secureRandom = SecureRandom()
        val nonce = StringBuilder(32)
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        repeat(32) {
            val index = secureRandom.nextInt(chars.length)
            nonce.append(chars[index])
        }
        return nonce.toString()
    }

    /**
     * 上传文件
     * @param url 上传目标URL
     * @param filePath 文件路径
     * @return 上传是否成功
     */
    @Throws(Exception::class)
    fun upload(url: String, filePath: String): Boolean {
        val file = File(filePath)
        val client = OkHttpClient()

        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        val request = Request.Builder()
            .header("Authorization", "Client-ID " + UUID.randomUUID().toString())
            .url(url)
            .addHeader("Content-Type", "application/octet-stream")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        return response.isSuccessful && response.code() == 200
    }

}