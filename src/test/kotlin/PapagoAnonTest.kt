package dev.jombi.copytotrans

import dev.jombi.copytotrans.translator.impl.newp.PapagoAnonTranslate

fun main() {
    val anon = PapagoAnonTranslate()
    println(anon.translate("hello"))
}