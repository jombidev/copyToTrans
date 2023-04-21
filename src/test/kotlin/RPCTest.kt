package dev.jombi.copytotrans

import dev.jombi.copytotrans.translator.impl.newg.GoogleRPCTranslate

fun main() {
    val e = GoogleRPCTranslate().translate("STORM\nGEFORCE")
    println(e)
}