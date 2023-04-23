package dev.jombi.copytotrans.translator.impl.old

import dev.jombi.copytotrans.buildUrlEncoded
import dev.jombi.copytotrans.config.mapper
import dev.jombi.copytotrans.translator.Translator
import java.net.HttpURLConnection
import java.net.URL

class GoogleAnonTranslate : Translator {
    val IP_REGEX = Regex("IP address: (.+?)<br>")
    val TIME_REGEX = Regex("Time: (.+?)<br>")
    val URL_REGEX = Regex("URL: (.+?)<br>")
    val AMP_REGEX = Regex("&amp;")
    val url = arrayOf(
        "https://translate.google.com/translate_a/single",
        "?client=at",
        "&dt=t",  // return sentences
        "&dt=rm", // add translit to sentences
        "&dj=1",  // result as pretty json instead of deep nested arrays;
    ).joinToString("")

    val from = "auto"
    val to = "ko"
    override fun translate(target: String): String {
        val params = buildUrlEncoded("sl" to from, "tl" to to, "q" to target)
        val con = URL(url).openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        con.setRequestProperty("User-Agent", "PostmanRuntime/7.31.1")
        con.doOutput = true
        con.doInput = true
        con.outputStream.use {
            it.write(params.toByteArray())
            it.flush()
        }
        con.connect()

        return if (con.responseCode !in 200..299) {
            val base = String(con.errorStream.readBytes())
            println(base)
            if (con.responseCode == 429) "RateLimited : ${buildManyRequest(base)}" else "Status: ${con.responseCode}"
        } else {
            val t = mapper.readTree(con.inputStream)
            if (!t.has("sentences")) ""
            else t["sentences"].first()["trans"].asText()
        }
    }

    fun buildManyRequest(string: String): String {
        val ip = IP_REGEX.find(string)?.groupValues?.firstOrNull() ?: ""
        val time = TIME_REGEX.find(string)?.groupValues?.firstOrNull() ?: ""
        val url = (URL_REGEX.find(string)?.groupValues?.firstOrNull() ?: "").replace(AMP_REGEX, "&")
        return "ip = $ip, time = $time, url = $url"
    }
}