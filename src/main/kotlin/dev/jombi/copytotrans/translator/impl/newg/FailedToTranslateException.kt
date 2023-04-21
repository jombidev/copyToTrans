package dev.jombi.copytotrans.translator.impl.newg

class FailedToTranslateException(code: Int) : RuntimeException("Failed to translate: $code received.") {
}