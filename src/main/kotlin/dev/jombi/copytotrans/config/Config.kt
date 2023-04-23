package dev.jombi.copytotrans.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.io.path.*

val mapper = jacksonObjectMapper()

fun getConfig(): Property = mapper.readValue(Path("config.json").inputStream())
private fun getRawConfig(): JsonNode = mapper.readTree(Path("config.json").apply { if (!exists() || !isRegularFile()) { createFile();writeText("{}") } }.inputStream())

fun makeDefaultConfig() {
    val cfg = getRawConfig()
    val path = Path("config.json")
    val p = Property()
//    if (cfg.hasNonNull("papagoKey")) p.papagoKey = cfg["papagoKey"].asText()
//    if (cfg.hasNonNull("papagoSecret")) p.papagoSecret = cfg["papagoSecret"].asText()
    if (cfg.hasNonNull("sound")) p.sound = cfg["sound"].asBoolean()
    if (cfg.hasNonNull("overlay")) p.overlay = cfg["overlay"].asBoolean()
    mapper.writerWithDefaultPrettyPrinter().writeValue(path.outputStream(), p)
}

//fun getPapagoApiKey(): String = getConfig().papagoKey
//fun getPapagoApiSecret(): String = getConfig().papagoSecret

fun shouldShowOverlay(): Boolean = getConfig().overlay
fun shouldPlaySound(): Boolean = getConfig().sound