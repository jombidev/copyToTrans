package dev.jombi.copytotrans.translator.impl.newg

class FailedToTranslateException : RuntimeException {
    constructor(code: Int, msg: String) : super("Failed to translate: $msg ($code)")
    constructor(code: Int) : super("Failed to translate: $code received.")
}