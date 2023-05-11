package dev.jombi.copytotrans.overlay.finish

import dev.jombi.copytotrans.config.shouldShowOverlay

object OverlayManager {
    private val ts = TranslateStatus()
    fun show(text: String) {
        if (shouldShowOverlay()) {
            ts.transResult.text = text
            ts.hideMs = System.currentTimeMillis() + 5000L
            ts.showOverlay()
        }
    }
    fun init() {
        ts.delayedHideLoopback()
        ts.draw()
    }
}