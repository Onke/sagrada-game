package za.nmu.wrpv.sagradaclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Locale;

import nmu.sagrada.core.Colour;

/**
 * Die colours and pip drawing, shared by the board, the draft pool and the practice tutorial so
 * every die in the app looks identical.
 */
final class Dice {

    /** Pip offsets on a -1..1 grid, indexed by die value. */
    private static final int[][] PIPS = {
            {},
            {0, 0},
            {-1, -1, 1, 1},
            {-1, -1, 0, 0, 1, 1},
            {-1, -1, 1, -1, -1, 1, 1, 1},
            {-1, -1, 1, -1, 0, 0, -1, 1, 1, 1},
            {-1, -1, 1, -1, -1, 0, 1, 0, -1, 1, 1, 1}};

    private Dice() {}

    static int colour(Context context, Colour colour) {
        int id;
        switch (colour) {
            case BLUE: id = R.color.dieBlue; break;
            case GREEN: id = R.color.dieGreen; break;
            case PURPLE: id = R.color.diePurple; break;
            case RED: id = R.color.dieRed; break;
            case YELLOW: id = R.color.dieYellow; break;
            default: id = R.color.outline; break;
        }
        return context.getResources().getColor(id, null);
    }

    /** Turns a protocol die id such as {@code BLUE3} into its colour, or null when unplaced. */
    static Colour colourOf(String dieId) {
        if (!isDie(dieId)) return null;
        try {
            return Colour.valueOf(dieId.substring(0, dieId.length() - 1));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static int valueOf(String dieId) {
        if (!isDie(dieId)) return 0;
        char last = dieId.charAt(dieId.length() - 1);
        return last >= '1' && last <= '6' ? last - '0' : 0;
    }

    static boolean isDie(String dieId) {
        return dieId != null && dieId.length() >= 2 && !"NULL".equals(dieId);
    }

    static String describe(Context context, String dieId) {
        Colour colour = colourOf(dieId);
        if (colour == null) return "";
        return name(colour) + " " + valueOf(dieId);
    }

    /** "RED" -> "Red", for anything a player reads. */
    static String name(Colour colour) {
        String lower = colour.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    /**
     * Draws the pips for {@code value} centred on {@code cx, cy} inside a square of {@code size}.
     * The caller sets the paint, so this works for both filled pips and outlined restriction pips.
     */
    static void drawPips(Canvas canvas, float cx, float cy, float size, int value, Paint paint) {
        if (value < 1 || value > 6) return;
        int[] offsets = PIPS[value];
        float step = size * 0.26f;
        float radius = size * 0.082f;
        for (int i = 0; i < offsets.length; i += 2) {
            canvas.drawCircle(cx + offsets[i] * step, cy + offsets[i + 1] * step, radius, paint);
        }
    }
}
