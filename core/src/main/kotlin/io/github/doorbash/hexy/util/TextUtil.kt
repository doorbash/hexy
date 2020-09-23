package io.github.doorbash.hexy.util

/**
 * Created by Milad Doorbash on 8/18/2019.
 */
object TextUtil {
    fun padLeftZeros(inputString: String, length: Int): String {
        if (inputString.length >= length) {
            return inputString
        }
        val sb = StringBuilder()
        while (sb.length < length - inputString.length) {
            sb.append('0')
        }
        sb.append(inputString)
        return sb.toString()
    }

    fun validateName(name: String): String {
        var ret = name.replace("\\s+".toRegex(), "")
        if (ret.length > 15) ret = ret.substring(0, 15)
        return ret
    }
}