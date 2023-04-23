package dev.jombi.copytotrans.translator.impl.newp

object PapagoUUIDGen {
    private var stored: String? = null
    fun gen(): String {
        if (stored != null) return stored!!
        var a = System.currentTimeMillis()
        val genned = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".mapIndexed { i, it ->
            if (it in "xy") {
                val rng = Math.random()
                val thing = ((a + 16 * rng) % 16).toInt()
                a /= 16
                (if ('x' == it) thing else 3 and thing or 8).toString(16)
            } else it
        }.joinToString("")
        stored = genned
        return stored!!
    }
}