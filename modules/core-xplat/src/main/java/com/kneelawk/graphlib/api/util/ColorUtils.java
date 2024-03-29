package com.kneelawk.graphlib.api.util;

/**
 * Color utilities.
 */
public final class ColorUtils {
    private ColorUtils() {
    }

    /**
     * Converts rgba floats into an argb int.
     *
     * @param red   the red value.
     * @param green the green value.
     * @param blue  the blue value.
     * @param alpha the alpha value.
     * @return the color integer in argb form.
     */
    public static int float2Argb(float red, float green, float blue, float alpha) {
        return ((int) (blue * 255f + 0f) & 0xFF)
            | (((int) (green * 255f + 0f) & 0xFF) << 0x8)
            | (((int) (red * 255f + 0f) & 0xFF) << 0x10)
            | (((int) (alpha * 255f + 0f) & 0xFF) << 0x18);
    }

    /**
     * Converts hsba floats into an argb int.
     *
     * @param hue        the hue value.
     * @param saturation the saturation value.
     * @param brightness the brightness value.
     * @param alpha      the alpha value.
     * @return the color as an integer in argb form.
     */
    public static int hsba2Argb(float hue, float saturation, float brightness, float alpha) {
        if (saturation == 0f) {
            return float2Argb(brightness, brightness, brightness, alpha);
        } else {
            float sector = (hue % 1f) * 6f;
            if (sector < 0) {
                sector += 6f;
            }

            float offset = sector - (int) sector;
            float off = brightness * (1f - saturation);
            float fadeOut = brightness * (1f - (saturation * offset));
            float fadeIn = brightness * (1f - (saturation * (1f - offset)));

            return switch ((int) sector % 6) {
                case 0 -> float2Argb(brightness, fadeIn, off, alpha);
                case 1 -> float2Argb(fadeOut, brightness, off, alpha);
                case 2 -> float2Argb(off, brightness, fadeIn, alpha);
                case 3 -> float2Argb(off, fadeOut, brightness, alpha);
                case 4 -> float2Argb(fadeIn, off, brightness, alpha);
                case 5 -> float2Argb(brightness, off, fadeOut, alpha);
                default -> throw new AssertionError();
            };
        }
    }
}
