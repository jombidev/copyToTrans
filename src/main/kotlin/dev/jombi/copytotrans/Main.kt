package dev.jombi.copytotrans

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import dev.jombi.copytotrans.config.makeDefaultConfig
import dev.jombi.copytotrans.config.shouldPlaySound
import dev.jombi.copytotrans.overlay.OverlayManager
import dev.jombi.copytotrans.translator.impl.newg.FailedToTranslateException
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineEvent
import kotlin.system.exitProcess


fun main() {
    makeDefaultConfig()
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val google = Keybinding(listOf(NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_Q)) {
        val target = clipboard.getData(DataFlavor.stringFlavor) as String
        println("Received: $target")
        try {
            val translated = Translators.Google.translate(target)
            println("Translated: $translated, using 'Google' translator")
            val sel = StringSelection(translated)
            clipboard.setContents(sel, sel)
            OverlayManager.show(translated)
            playFinishedSound()
        } catch (e: FailedToTranslateException) {
            println("Failed to translate: ${e.message}")
            val sel = StringSelection(e.message)
            clipboard.setContents(sel, sel)

        }
    }
    val papago = Keybinding(listOf(NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_W)) {
        val target = clipboard.getData(DataFlavor.stringFlavor) as String
        println("Received: $target")
        val translated = Translators.Papago.translate(target)
        println("Translated: $translated, using 'Papago' translator")
        val sel = StringSelection(translated)
        clipboard.setContents(sel, sel)
        OverlayManager.show(translated)
        playFinishedSound()
    }
    val exitKey = Keybinding(listOf(NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_ESCAPE)) {
        GlobalScreen.unregisterNativeHook()
        exitProcess(0)
    }
    val keybindings = arrayOf(google, papago, exitKey)
    GlobalScreen.registerNativeHook()
    GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
        override fun nativeKeyPressed(e: NativeKeyEvent) {
            for (k in keybindings) k.press(e.keyCode)
        }

        override fun nativeKeyReleased(e: NativeKeyEvent) {
            for (k in keybindings) k.unpress(e.keyCode)
        }
    })
    OverlayManager.init()
}

fun playFinishedSound() {
    if (!shouldPlaySound()) return
    try {
        val stream = AudioSystem.getAudioInputStream(BufferedInputStream(ClassLoader.getSystemResourceAsStream("sharexfinish.wav")!!))
        val format = stream.format
        val info = DataLine.Info(Clip::class.java, format)
        val clip = AudioSystem.getLine(info) as Clip
        clip.open(stream)
        clip.start()
        clip.addLineListener { if (it.type == LineEvent.Type.STOP) clip.close() }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}