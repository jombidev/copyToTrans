package dev.jombi.copytotrans

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import dev.jombi.copytotrans.translator.impl.newg.FailedToTranslateException
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import kotlin.system.exitProcess

fun main() {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val google = Keybinding(listOf(NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_Q)) {
        val target = clipboard.getData(DataFlavor.stringFlavor) as String
        println("Received: $target")
        try {
            val translated = Translators.Google.translate(target)
            println("Translated: $translated, using 'Google' translator")
            val sel = StringSelection(translated)
            clipboard.setContents(sel, sel)
        } catch (e: FailedToTranslateException) {
            println("Failed to translate: ${e.message}")
            val sel = StringSelection(e.message)
            clipboard.setContents(sel, sel)

        }
    }
    val googleOld = Keybinding(listOf(NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_ALT, NativeKeyEvent.VC_Q)) {
        val target = clipboard.getData(DataFlavor.stringFlavor) as String
        println("Received: $target")
        val translated = Translators.GoogleOld.translate(target)
        println("Translated: $translated, using 'Google Old' translator")
        val sel = StringSelection(translated)
        clipboard.setContents(sel, sel)
    }
    val papago = Keybinding(listOf(NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_W)) {
        val target = clipboard.getData(DataFlavor.stringFlavor) as String
        println("Received: $target")
        val translated = Translators.Papago.translate(target)
        println("Translated: $translated, using 'Papago' translator")
        val sel = StringSelection(translated)
        clipboard.setContents(sel, sel)
    }
    val exitKey = Keybinding(listOf(NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_ESCAPE)) {
        GlobalScreen.unregisterNativeHook()
        exitProcess(0)
    }
    val keybindings = arrayOf(google, googleOld, papago, exitKey)
    GlobalScreen.registerNativeHook()
    GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
        override fun nativeKeyPressed(e: NativeKeyEvent) {
            for (k in keybindings) k.press(e.keyCode)
        }

        override fun nativeKeyReleased(e: NativeKeyEvent) {
            for (k in keybindings) k.unpress(e.keyCode)
        }
    })
}