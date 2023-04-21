package dev.jombi.copytotrans

class Keybinding(combos: List<Int>, private val action: () -> Unit) {
    private val MASK_A = 1 shl 0
    private val MASK_B = 1 shl 1
    private val MASK_C = 1 shl 2
    private val MASK_D = 1 shl 3
    private val MASK_E = 1 shl 4
    private val MASK_F = 1 shl 5
    private val MASK_G = 1 shl 6
    private val MASK_H = 1 shl 7

    private val KEY_1 = MASK_A
    private val KEY_2 = MASK_A or MASK_B
    private val KEY_3 = MASK_A or MASK_B or MASK_C
    private val KEY_4 = MASK_A or MASK_B or MASK_C or MASK_D
    private val KEY_5 = MASK_A or MASK_B or MASK_C or MASK_D or MASK_E
    private val KEY_6 = MASK_A or MASK_B or MASK_C or MASK_D or MASK_E or MASK_F
    private val KEY_7 = MASK_A or MASK_B or MASK_C or MASK_D or MASK_E or MASK_F or MASK_G
    private val KEY_8 = MASK_A or MASK_B or MASK_C or MASK_D or MASK_E or MASK_F or MASK_G or MASK_H

    var keyPressDetector = 0x00000000
        private set
    private var comboArray = combos.toTypedArray()

    init {
        if (combos.size !in 1..8) {
            throw IllegalArgumentException("Failed to init: 'combos' is too small or too large.")
        }
    }
    private val comboSize = combos.size

    fun press(keyCode: Int) {
        if (comboArray.indexOf(keyCode) != -1) {
            when (comboArray.indexOf(keyCode)) {
                0 -> keyPressDetector = keyPressDetector or MASK_A
                1 -> keyPressDetector = keyPressDetector or MASK_B
                2 -> keyPressDetector = keyPressDetector or MASK_C
                3 -> keyPressDetector = keyPressDetector or MASK_D
                4 -> keyPressDetector = keyPressDetector or MASK_E
                5 -> keyPressDetector = keyPressDetector or MASK_F
                6 -> keyPressDetector = keyPressDetector or MASK_G
                7 -> keyPressDetector = keyPressDetector or MASK_H
            }
        }
        when (comboSize) {
            1 -> if (keyPressDetector == KEY_1) {
                action()
                //println("pressed action")
            }
            2 -> if (keyPressDetector == KEY_2) {
                action()
                //println("pressed action")
            }
            3 -> if (keyPressDetector == KEY_3) {
                action()
                //println("pressed action")
            }
            4 -> if (keyPressDetector == KEY_4) {
                action()
                //println("pressed action")
            }
            5 -> if (keyPressDetector == KEY_5) {
                action()
                //println("pressed action")
            }
            6 -> if (keyPressDetector == KEY_6) {
                action()
                //println("pressed action")
            }
            7 -> if (keyPressDetector == KEY_7) {
                action()
                //println("pressed action")
            }
            8 -> if (keyPressDetector == KEY_8) {
                action()
                //println("pressed action")
            }
        }
    }

    fun unpress(keyCode: Int) {
        if (comboArray.indexOf(keyCode) != -1) {
            when (comboArray.indexOf(keyCode)) {
                0 -> keyPressDetector = keyPressDetector xor MASK_A
                1 -> keyPressDetector = keyPressDetector xor MASK_B
                2 -> keyPressDetector = keyPressDetector xor MASK_C
                3 -> keyPressDetector = keyPressDetector xor MASK_D
                4 -> keyPressDetector = keyPressDetector xor MASK_E
                5 -> keyPressDetector = keyPressDetector xor MASK_F
                6 -> keyPressDetector = keyPressDetector xor MASK_G
                7 -> keyPressDetector = keyPressDetector xor MASK_H
            }
        }
    }

}