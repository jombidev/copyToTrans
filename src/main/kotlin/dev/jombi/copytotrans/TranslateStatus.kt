package dev.jombi.copytotrans

import java.awt.AWTException
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Toolkit
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

class TranslateStatus : JDialog() {
    private val lab: JLabel
    var hideMs = 0L
    val transResult: JLabel
    init {
        isUndecorated = true
        isAlwaysOnTop = true
        isAutoRequestFocus = false
        isFocusable = false
        focusableWindowState = false
        defaultCloseOperation = DISPOSE_ON_CLOSE

        opacity = 0.75f
        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.background = Color(1f, 1f, 1f)
        lab = JLabel("Translation finished")
        panel.add(lab, BorderLayout.NORTH)
        transResult = JLabel("text")
        panel.add(transResult, BorderLayout.SOUTH)
        add(panel)
    }

    fun delayedHideLoopback() {
        Thread {
            while (true) {
                if (hideMs - System.currentTimeMillis() < 0L) hideOverlay()
                Thread.sleep(10L)
            }
        }.start()
    }

    fun draw() {
        SwingUtilities.invokeLater {
            try {
                val screenSize = Toolkit.getDefaultToolkit().screenSize
                setLocation(screenSize.width / 2 - lab.width, screenSize.height / 2 + 32)
//                showOverlay()
                pack()
            } catch (e: AWTException) {
//                error("Error while showing overlay", e)
                exitProcess(-1)
            }
        }
    }

    fun showOverlay() {
        if (!isVisible) isVisible = true
    }
    fun hideOverlay() {
        if (isVisible) isVisible = false
    }

}