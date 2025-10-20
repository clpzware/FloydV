package fr.ambient.util.math;

public class MathsUtil {
    public static double interpolate(final double old, final double now, final float partialTicks) {
        return interpolate((float) old, (float) now, partialTicks);
    }

    public static float interpolate(final float old, final float now, final float partialTicks) {
        return old + (now - old) * partialTicks;
    }
}