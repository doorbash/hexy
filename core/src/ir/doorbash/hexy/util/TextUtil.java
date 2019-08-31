package ir.doorbash.hexy.util;

/**
 * Created by Milad Doorbash on 8/18/2019.
 */
public class TextUtil {
    public static String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    public static String validateName(String name) {
        String ret = name.replaceAll("\\s+", "");
        if (ret.length() > 15) ret = ret.substring(0, 15);
        return ret;
    }
}
