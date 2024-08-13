package dev.jombi.copytotrans.translator.impl.newp

import org.jsoup.Jsoup
import java.net.URL

object PapagoSecretGen {
    private var hash: String? = null
    fun gen(): String {
        if (hash == null) {
            val soup = Jsoup.parse(URL(PapagoAnonTranslate.ENDPOINT), 0)
            val src = soup.body().getElementsByTag("script")
                .find { it.attr("src").startsWith("/main") }!!.attr("src")

            val js = String(URL("${PapagoAnonTranslate.ENDPOINT}${src}").openStream().readBytes())
            val ppg = js.indexOf("PPG ") - 2
            val authorizationBuilder = js.substring(ppg, js.indexOf("toString", ppg))
            val hash = authorizationBuilder.split('"').let { it[it.lastIndex - 1] }
            this.hash = hash
        }
        println(hash)
        return hash!!
    }
}