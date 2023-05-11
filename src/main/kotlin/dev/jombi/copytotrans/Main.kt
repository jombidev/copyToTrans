package dev.jombi.copytotrans

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import dev.jombi.copytotrans.config.makeDefaultConfig
import dev.jombi.copytotrans.exception.TraySupportException
import dev.jombi.copytotrans.overlay.finish.OverlayManager
import dev.jombi.copytotrans.overlay.setting.ConfigGUI
import dev.jombi.copytotrans.sound.Sounds
import dev.jombi.copytotrans.translator.Translators
import dev.jombi.copytotrans.translator.impl.newg.FailedToTranslateException
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun isMac(): Boolean {
    val os = System.getProperty("os.name").lowercase()
    return os.contains("mac") || os.contains("darwin")
}

fun main() {
    makeDefaultConfig()
    trayIcon
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val mod = if (isMac()) NativeKeyEvent.VC_CONTROL else NativeKeyEvent.VC_ALT
    val google = Keybinding(listOf(mod, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_Q)) {
        try {
            val target = clipboard.getData(DataFlavor.stringFlavor) as String
            println("Received: $target")
            val translated = Translators.Google.translate(target)
            println("Translated: $translated, using 'Google' translator")
            val sel = StringSelection(translated)
            clipboard.setContents(sel, sel)
            OverlayManager.show(translated)
            Sounds.SUCCESS.play()
        } catch (e: Exception) {
            println("Failed to translate: ${e.message}")
            val sel = StringSelection(e.message)
            clipboard.setContents(sel, sel)
            Sounds.FAILED.play()
            if (e !is FailedToTranslateException) e.printStackTrace()
        }
    }
    val papago = Keybinding(listOf(mod, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_W)) {
        try {
            val target = clipboard.getData(DataFlavor.stringFlavor) as String
            println("Received: $target")
            val translated = Translators.Papago.translate(target)
            println("Translated: $translated, using 'Papago' translator")
            val sel = StringSelection(translated)
            clipboard.setContents(sel, sel)
            OverlayManager.show(translated)
            Sounds.SUCCESS.play()
        } catch (e: Exception) {
            println("Failed to translate: ${e.message}")
            val sel = StringSelection(e.message)
            clipboard.setContents(sel, sel)
            Sounds.FAILED.play()
            if (e !is FailedToTranslateException) e.printStackTrace()
        }
    }
    val deepl = Keybinding(listOf(mod, NativeKeyEvent.VC_SHIFT, NativeKeyEvent.VC_E)) {
//        val target = clipboard.getData(DataFlavor.stringFlavor) as String
//        println("Received: $target")
//        val translated = Translators.DeepL.translate(target)
//        println("Translated: $translated, using 'Papago' translator")
//        val sel = StringSelection(translated)
//        clipboard.setContents(sel, sel)
//        OverlayManager.show(translated)
//        playFinishedSound()
    }
    val exitKey = Keybinding(listOf(NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_ESCAPE)) {
        GlobalScreen.unregisterNativeHook()
        exitProcess(0)
    }
    val keybindings = arrayOf(google, papago, deepl, exitKey)
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

private var _trayIcon: TrayIcon? = null
val trayIcon: TrayIcon
    get() {
        if (!SystemTray.isSupported())
            throw TraySupportException
        if (_trayIcon == null) {
            val t = TrayIcon(ImageIO.read(ClassLoader.getSystemResourceAsStream("image.png")!!))
            t.isImageAutoSize = true
            t.toolTip = "CopyToTrans"
            t.addActionListener {
                ConfigGUI.display()
            }
            _trayIcon = t
            SystemTray.getSystemTray().add(_trayIcon!!)
        }
        return _trayIcon!!
    }