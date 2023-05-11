package dev.jombi.copytotrans.overlay.setting

import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.border.TitledBorder


object ConfigGUI : JFrame() {
    init {
        val sound = JCheckBox("Play Finish Sound")
        val gui = JCheckBox("Display Finish Overlay")
        val panel = JPanel()
        panel.border = TitledBorder(null, "Finish Options", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panel.toolTipText = ""
        panel.setBounds(0, 0, 400, 300) //panel size setting
        panel.add(sound)
        panel.add(gui)
        contentPane.add(panel)
    }
    fun display() {
        if (!isVisible) isVisible = true
    }
    fun revoke() {
        if (isVisible) isVisible = false
    }
}