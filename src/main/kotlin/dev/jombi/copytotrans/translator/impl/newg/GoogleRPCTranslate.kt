package dev.jombi.copytotrans.translator.impl.newg

import dev.jombi.copytotrans.config.mapper
import dev.jombi.copytotrans.translator.Translator
import dev.jombi.copytotrans.translator.buildUrlEncoded
import java.net.HttpURLConnection
import java.net.URL

const val TRANSLATE_RPC = "https://translate.google.com/_/TranslateWebserverUi/data/batchexecute"
const val RPC_ID = "MkEWBc"

class GoogleRPCTranslate : Translator {
    fun buildRpcRequest(text: String, src: String, dst: String): String {
        val dump = mapper.writeValueAsString(arrayOf(arrayOf(text, src, dst, true), arrayOf<Any?>(null)))
        return mapper.writeValueAsString(arrayOf(arrayOf(arrayOf(RPC_ID, dump, null, "generic"))))
    }
    fun _translate(text: String, src: String, dst: String): String {
        val data = buildUrlEncoded("f.req" to buildRpcRequest(text, src, dst))
        val params = mapOf(
            "rpcids" to RPC_ID,
            "bl" to "boq_translate-webserver_20201207.13_p0",
            "soc-app" to "1",
            "soc-platform" to "1",
            "soc-device" to "1",
            "rt" to "c",
            "Content-Type" to "application/x-www-form-urlencoded;charset=utf-8"
        )
        val con = URL(TRANSLATE_RPC).openConnection() as HttpURLConnection
        con.doInput = true
        con.doOutput = true
        con.requestMethod = "POST"
        for ((k, v) in params) con.addRequestProperty(k, v)
        con.outputStream.use {
            it.write(data.toByteArray())
            it.flush()
        }
        con.connect()

        if (con.responseCode != 200)
            throw FailedToTranslateException(con.responseCode)

        return String(con.inputStream.readBytes())
    }

    override fun translate(target: String): String {
        val trans = _translate(target, "auto", "ko")
        var tokenFound = false
        val squareBracketCounts = arrayOf(0, 0)
        var resp = ""
        val data = trans.split('\n')
        for (line in data) {
            tokenFound = tokenFound || "\"${RPC_ID}\"" in line.substring(0, minOf(line.length, 30))
            if (!tokenFound) continue

            var isInString = false

            for ((index, char) in line.withIndex()) {
                if (char == '"' && line[maxOf(0, index - 1)] != '\\') isInString = !isInString
                if (!isInString) {
                    if (char == '[') squareBracketCounts[0]++
                    if (char == ']') squareBracketCounts[1]++
                }
            }
            resp += line
            if (squareBracketCounts[0] == squareBracketCounts[1]) break
        }


        val dataThing = mapper.readTree(resp)
        val parsed = mapper.readTree(dataThing[0][2].asText())
        val l = arrayListOf<String>()
        println(dataThing)
        for (node in parsed[1][0][0][5]) {
            if (node.size() < 2) continue
            l.add(node[0].asText())
        }
        return l.joinToString("\n")

    }
}