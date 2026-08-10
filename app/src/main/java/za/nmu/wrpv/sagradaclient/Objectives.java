package za.nmu.wrpv.sagradaclient;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import nmu.sagrada.core.Colour;
import nmu.sagrada.core.ObjectiveScorer;

/**
 * Plain-English descriptions of every objective the server can deal, so a player never has to
 * guess what a card rewards. Titles and point values mirror {@link ObjectiveScorer} exactly.
 */
final class Objectives {

    private static final class Entry {
        final int title;
        final int body;
        final int points;
        final int image;

        Entry(int title, int body, int points, int image) {
            this.title = title;
            this.body = body;
            this.points = points;
            this.image = image;
        }
    }

    private static final Map<String, Entry> CATALOGUE = new HashMap<>();

    static {
        CATALOGUE.put(ObjectiveScorer.UPSIDE_DOWN_NUMBER_PYRAMID, new Entry(R.string.obj_pyramid,
                R.string.obj_pyramid_body, R.string.obj_points_fixed, R.drawable.public_objective_card1));
        CATALOGUE.put(ObjectiveScorer.THREE_FOUR_PAIR, new Entry(R.string.obj_three_four,
                R.string.obj_three_four_body, R.string.obj_points_pair, R.drawable.public_objective_card2));
        CATALOGUE.put(ObjectiveScorer.DIFFERENT_NUMBERS_COLUMN, new Entry(R.string.obj_diff_numbers_column,
                R.string.obj_diff_numbers_column_body, R.string.obj_points_column_number, R.drawable.public_objective_card3));
        CATALOGUE.put(ObjectiveScorer.DIFFERENT_COLOURS_COLUMN, new Entry(R.string.obj_diff_colours_column,
                R.string.obj_diff_colours_column_body, R.string.obj_points_column_colour, R.drawable.public_objective_card4));
        CATALOGUE.put(ObjectiveScorer.FIVE_SIX_PAIR, new Entry(R.string.obj_five_six,
                R.string.obj_five_six_body, R.string.obj_points_pair, R.drawable.public_objective_card5));
        CATALOGUE.put(ObjectiveScorer.JAGGED_DIFFERENT_COLOURS, new Entry(R.string.obj_jagged,
                R.string.obj_jagged_body, R.string.obj_points_set, R.drawable.public_objective_card6));
        CATALOGUE.put(ObjectiveScorer.DIFFERENT_COLOURS_ROW, new Entry(R.string.obj_diff_colours_row,
                R.string.obj_diff_colours_row_body, R.string.obj_points_row, R.drawable.public_objective_card7));
        CATALOGUE.put(ObjectiveScorer.LIGHT_SHADES, new Entry(R.string.obj_light,
                R.string.obj_light_body, R.string.obj_points_fixed, R.drawable.public_objective_card8));
        CATALOGUE.put(ObjectiveScorer.ONE_TWO, new Entry(R.string.obj_one_two,
                R.string.obj_one_two_body, R.string.obj_points_pair, R.drawable.public_objective_card9));
        CATALOGUE.put(ObjectiveScorer.DIFFERENT_NUMBERS_ROW, new Entry(R.string.obj_diff_numbers_row,
                R.string.obj_diff_numbers_row_body, R.string.obj_points_row_number, R.drawable.public_objective_card10));
    }

    private Objectives() {}

    static String title(Context context, String objective) {
        Entry entry = CATALOGUE.get(objective);
        return context.getString(entry == null ? R.string.obj_unknown : entry.title);
    }

    static String body(Context context, String objective) {
        Entry entry = CATALOGUE.get(objective);
        return context.getString(entry == null ? R.string.obj_unknown_body : entry.body);
    }

    static String points(Context context, String objective) {
        Entry entry = CATALOGUE.get(objective);
        return entry == null ? "" : context.getString(entry.points);
    }

    static int image(String objective) {
        Entry entry = CATALOGUE.get(objective);
        return entry == null ? R.drawable.objectives : entry.image;
    }

    /** The private objective is a colour name rather than an objective id. */
    static Colour privateColour(String privateObjective) {
        try {
            return Colour.valueOf(privateObjective);
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return null;
        }
    }

    static String privateTitle(Context context, String privateObjective) {
        Colour colour = privateColour(privateObjective);
        return colour == null ? context.getString(R.string.obj_unknown)
                : context.getString(R.string.private_objective_short, Dice.name(colour));
    }

    static String privateBody(Context context, String privateObjective) {
        Colour colour = privateColour(privateObjective);
        return colour == null ? context.getString(R.string.obj_unknown_body)
                : context.getString(R.string.private_objective_detail, Dice.name(colour).toLowerCase());
    }
}
