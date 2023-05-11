package dev.jombi.copytotrans.translator.impl.old

import dev.jombi.copytotrans.translator.buildUrlEncoded
import dev.jombi.copytotrans.config.mapper
import dev.jombi.copytotrans.translator.Translator
import java.net.HttpURLConnection
import java.net.URL

class PapagoTokenedTranslate : Translator {
    val from = "auto"
    val to = "ko"
    override fun translate(target: String): String {
        val con = URL("https://openapi.naver.com/v1/papago/n2mt").openConnection() as HttpURLConnection
        con.doOutput = true
        con.doInput = true
        con.requestMethod = "POST"
//        con.addRequestProperty("X-Naver-Client-Id", getPapagoApiKey())
//        con.addRequestProperty("X-Naver-Client-Secret", getPapagoApiSecret())
        val body = buildUrlEncoded("source" to from, "target" to to, "text" to target)
        con.outputStream.use {
            it.write(body.toByteArray())
            it.flush()
        }
        con.connect()
        return if (con.responseCode !in 200..299) {
            val err = mapper.readTree(con.errorStream)
            if (err.has("errorMessage")) err["errorMessage"].asText() else "${con.responseCode}"
        } else {
            val res = mapper.readTree(con.inputStream)
            res["message"]["result"]["translatedText"].asText()
        }
    }
}