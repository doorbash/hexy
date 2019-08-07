package ir.doorbash.hexy.util;

/**
 * Created by Milad Doorbash on 8/7/2019.
 */
public class _Math {
    public static float angleDistance(float a, float b) {
        float d = b - a;
        while (d < -Math.PI) {
            d += 2 * Math.PI;
        }
        while (d > Math.PI) {
            d -= 2 * Math.PI;
        }
        return d;
    }
}
