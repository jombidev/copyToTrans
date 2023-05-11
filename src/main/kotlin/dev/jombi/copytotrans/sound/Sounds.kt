package dev.jombi.copytotrans.sound

import dev.jombi.copytotrans.config.shouldPlaySound
import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineEvent

enum class Sounds(private val fileName: String) {
    SUCCESS("TaskCompleted.wav"), FAILED("ErrorSound.wav");
    fun play() {
        if (!shouldPlaySound()) return
        try {
            val stream = AudioSystem.getAudioInputStream(BufferedInputStream(ClassLoader.getSystemResourceAsStream(fileName)!!))
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
}