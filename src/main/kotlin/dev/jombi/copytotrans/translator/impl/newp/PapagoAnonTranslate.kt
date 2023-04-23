package dev.jombi.copytotrans.translator.impl.newp

import dev.jombi.copytotrans.buildUrlEncoded
import dev.jombi.copytotrans.config.mapper
import dev.jombi.copytotrans.translator.Translator
import dev.jombi.copytotrans.translator.impl.newg.FailedToTranslateException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.random.Random

class PapagoAnonTranslate : Translator {
    private val endpoint = "https://papago.naver.com"
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/114.0"
    val headers = hashMapOf<String, String>()
    private val TIMESTAMP_REGEX = Regex("var query= null,timestamp= ([0-9]+)")
    private val timestampFromHtml: Long
    private val delayedTimestamp: Long
    private val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
        override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
        }

        override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return null
        }

    })

    init {
        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        val con = URL(endpoint).openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.addRequestProperty("User-Agent", userAgent)
        con.connect()
        val (jsess, value) = con.getHeaderField("set-cookie").split(';')[0].split('=')
        headers[jsess] = value
        headers["papago_skin_locale"] = "en"
        delayedTimestamp = System.currentTimeMillis() + Random.nextInt(200, 800)
        timestampFromHtml =
            TIMESTAMP_REGEX.find(String(con.inputStream.readBytes()))?.groupValues?.get(1)?.toLong() ?: delayedTimestamp

//        println(timestampFromHtml)
//        println(delayedTimestamp)
//        println(delayedTimestamp - timestampFromHtml)
        PapagoUUIDGen.gen()
    }

    override fun translate(target: String): String {
        val toTranslate = detect(target)
        val offset = System.currentTimeMillis() + timestampFromHtml - delayedTimestamp // (new Date).getTime() + a - d
        val url = "$endpoint/apis/n2mt/translate"
        val uuid = PapagoUUIDGen.gen()
        val hash = createAuthorization(offset, url)
        val transHeader = arrayOf(
            "Accept" to "application/json",
            "Accept-Language" to "en",
            "Authorization" to "PPG $uuid:$hash",
            "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "Timestamp" to "$offset",
            "device-type" to "pc",
            "x-apigw-partnerid" to "papago",
            "Cookie" to headers.toList().joinToString("; ") { "${it.first}=${URLEncoder.encode(it.second, "utf-8")}" },
            "User-Agent" to userAgent,
        )
        val body = buildUrlEncoded(
            "deviceId" to uuid,
            "locale" to "en",
            "dict" to "false",
            "honorific" to "false",
            "instant" to "false",
            "paging" to "false",
            "source" to toTranslate,
            "target" to "ko",
            "text" to target
        )
        val con = URL(url).openConnection() as HttpURLConnection
        for ((key, value) in transHeader) con.setRequestProperty(key, value)
        con.requestMethod = "POST"
        con.doOutput = true
        con.outputStream.use {
            it.write(body.toByteArray(charset("utf-8")))
            it.flush()
        }
        con.connect()
        if (con.responseCode in 200..299) {
//            return String(con.inputStream.readBytes())
            return mapper.readTree(con.inputStream)["translatedText"].asText()
        } else {
            println(String(con.errorStream.readBytes()))
            throw FailedToTranslateException(con.responseCode)
        }
    }

    fun detect(string: String): String {
        val offset = System.currentTimeMillis() + timestampFromHtml - delayedTimestamp // (new Date).getTime() + a - d
        val url = "$endpoint/apis/langs/dect"
        val uuid = PapagoUUIDGen.gen()
        val hash = createAuthorization(offset, url)
        val detectHeader = mapOf(
            "Accept" to "application/json",
            "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "device-type" to "pc",
            "Cookie" to headers.toList().joinToString("; ") { "${it.first}=${URLEncoder.encode(it.second, "utf-8")}" },
            "User-Agent" to userAgent,
            "Timestamp" to "$offset",
            "Authorization" to "PPG $uuid:$hash"
        )
        val con = URL(url).openConnection() as HttpURLConnection
        for ((key, value) in detectHeader) con.setRequestProperty(key, value)
        con.requestMethod = "POST"
        con.doOutput = true
        val body = buildUrlEncoded("query" to string)
        con.outputStream.use {
            it.write(body.toByteArray(charset("utf-8")))
            it.flush()
        }
        con.connect()
        if (con.responseCode in 200..299) {
            return mapper.readTree(con.inputStream)["langCode"].asText()
        } else {
            println(String(con.errorStream.readBytes()))
            throw FailedToTranslateException(con.responseCode)
        }
    }

    private fun createAuthorization(time: Long, url: String): String {
        val uuid = PapagoUUIDGen.gen()
        val mac = Mac.getInstance("HmacMD5")
        mac.init(SecretKeySpec("v1.7.3_de60216eaa".toByteArray(charset("utf-8")), "HmacMD5"))
        val myMac = uuid + "\n" + url.split("?")[0] + "\n" + time
        return Base64.getEncoder().encodeToString(mac.doFinal(myMac.toByteArray()))
    }
}