package dev.jombi.copytotrans.translator

import com.fasterxml.jackson.databind.ObjectMapper
import dev.jombi.copytotrans.translator.impl.newg.GoogleRPCTranslate
import dev.jombi.copytotrans.translator.impl.newp.PapagoAnonTranslate
import dev.jombi.copytotrans.translator.impl.old.GoogleAnonTranslate
import dev.jombi.copytotrans.translator.impl.old.PapagoTokenedTranslate
import java.net.URLEncoder

interface Translators {
    val translator: Translator
    fun translate(string: String): String = translator.translate(string)

    object Google : Translators {
        override val translator = GoogleRPCTranslate()
    }
    object GoogleOld : Translators {
        override val translator = GoogleAnonTranslate()
    }
    object PapagoOld : Translators {
        override val translator = PapagoTokenedTranslate()
    }
    object Papago : Translators {
        override val translator = PapagoAnonTranslate()
    }
}

fun ObjectMapper.buildJson(vararg pairs: Pair<String, Any?>): String = writeValueAsString(mapOf(*pairs))
fun buildUrlEncoded(vararg pairs: Pair<String, Comparable<*>>) = pairs.joinToString("&") { "${it.first}=${URLEncoder.encode("${it.second}", "utf-8")}" }