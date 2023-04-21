package dev.jombi.copytotrans

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.io.path.Path
import kotlin.io.path.inputStream

val mapper = ObjectMapper()

fun getRawConfig(): JsonNode = mapper.readTree(Path("config.json").inputStream())

fun getPapagoApiKey(): String = getRawConfig()["papagoKey"].asText()
fun getPapagoApiSecret(): String = getRawConfig()["papagoSecret"].asText()
