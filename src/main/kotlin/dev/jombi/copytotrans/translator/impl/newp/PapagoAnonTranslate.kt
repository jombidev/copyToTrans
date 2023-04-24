package dev.jombi.copytotrans.translator.impl.newp

import dev.jombi.copytotrans.buildUrlEncoded
import dev.jombi.copytotrans.config.mapper
import dev.jombi.copytotrans.translator.Translator
import dev.jombi.copytotrans.translator.impl.newg.FailedToTranslateException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PapagoAnonTranslate : Translator {
    private val endpoint = "https://papago.naver.com"
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/114.0"

    init {
        PapagoUUIDGen.gen()
    }

    override fun translate(target: String): String {
        val offset = System.currentTimeMillis() - 1500L
        val url = "$endpoint/apis/n2mt/translate"
        val uuid = PapagoUUIDGen.gen()
        val hash = createAuthorization(offset, url)
        val transHeader = arrayOf(
            "Authorization" to "PPG $uuid:$hash",
            "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "Timestamp" to "$offset",
            "User-Agent" to userAgent,
        )
        val body = buildUrlEncoded(
            "deviceId" to uuid,
            "locale" to "en",
            "honorific" to "false",
            "instant" to "false",
            "source" to "auto",
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
            return mapper.readTree(con.inputStream)["translatedText"].asText()
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