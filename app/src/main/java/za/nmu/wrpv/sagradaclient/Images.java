package za.nmu.wrpv.sagradaclient;

public class Images {
    public static int getImage(String id) {
        if(id == null)
            id = "";
        switch (id) {
            //Private Objectives
            case "BLUE":
                return R.drawable.blue;
            case "GREEN":
                return R.drawable.green;
            case "RED":
                return R.drawable.red;
            case "YELLOW":
                return R.drawable.yellow;
            case "PURPLE":
                return R.drawable.purple;

            //Public Objectives
            case "UPSIDE_DOWN_NUMBER_PYRAMID":
                return R.drawable.public_objective_card1;
            case "THREE_FOUR_PAIR":
                return R.drawable.public_objective_card2;
            case "DIFFERENT_NUMBERS_COLUMN":
                return R.drawable.public_objective_card3;
            case "DIFFERENT_COLOURS_COLUMN":
                return R.drawable.public_objective_card4;
            case "FIVE_SIX_PAIR":
                return R.drawable.public_objective_card5;
            case "JAGGED_DIFFERENT_COLOURS ":
                return R.drawable.public_objective_card6;
            case "DIFFERENT_COLOURS_ROW":
                return R.drawable.public_objective_card7;
            case "LIGHT_SHADES":
                return R.drawable.public_objective_card8;
            case "ONE_TWO":
                return R.drawable.public_objective_card9;
            case "DIFFERENT_NUMBERS_ROW":
                return R.drawable.public_objective_card10;

            //Windows
            case "WINDOW1":
                return R.drawable.window1;
            case "WINDOW2":
                return R.drawable.window2;
            case "WINDOW3":
                return R.drawable.window3;
            case "WINDOW4":
                return R.drawable.window4;
            case "WINDOW5":
                return R.drawable.window5;
            case "WINDOW6":
                return R.drawable.window6;
            case "WINDOW7":
                return R.drawable.window7;
            case "WINDOW8":
                return R.drawable.window8;
            case "WINDOW9":
                return R.drawable.window9;
            case "WINDOW10":
                return R.drawable.window10;
            case "WINDOW11":
                return R.drawable.window11;
            case "WINDOW12":
                return R.drawable.window12;
            case "WINDOW13":
                return R.drawable.window13;
            case "WINDOW14":
                return R.drawable.window14;
            case "WINDOW15":
                return R.drawable.window15;
            case "WINDOW16":
                return R.drawable.window16;
            case "WINDOW17":
                return R.drawable.window17;
            case "WINDOW18":
                return R.drawable.window18;
            case "WINDOW19":
                return R.drawable.window19;
            case "WINDOW20":
                return R.drawable.window20;
            case "WINDOW21":
                return R.drawable.window21;
            case "WINDOW22":
                return R.drawable.window22;
            case "WINDOW23":
                return R.drawable.window23;
            case "WINDOW24":
                return R.drawable.window24;
            case "WINDOW25":
                return R.drawable.window25;

            //Dice
            case "BLUE1":
                return R.drawable.blue_1;
            case "BLUE2":
                return R.drawable.blue_2;
            case "BLUE3":
                return R.drawable.blue_3;
            case "BLUE4":
                return R.drawable.blue_4;
            case "BLUE5":
                return R.drawable.blue_5;
            case "BLUE6":
                return R.drawable.blue_6;
            case "GREEN1":
                return R.drawable.green_1;
            case "GREEN2":
                return R.drawable.green_2;
            case "GREEN3":
                return R.drawable.green_3;
            case "GREEN4":
                return R.drawable.green_4;
            case "GREEN5":
                return R.drawable.green_5;
            case "GREEN6":
                return R.drawable.green_6;
            case "RED1":
                return R.drawable.red_1;
            case "RED2":
                return R.drawable.red_2;
            case "RED3":
                return R.drawable.red_3;
            case "RED4":
                return R.drawable.red_4;
            case "RED5":
                return R.drawable.red_5;
            case "RED6":
                return R.drawable.red_6;
            case "PURPLE1":
                return R.drawable.purple_1;
            case "PURPLE2":
                return R.drawable.purple_2;
            case "PURPLE3":
                return R.drawable.purple_3;
            case "PURPLE4":
                return R.drawable.purple_4;
            case "PURPLE5":
                return R.drawable.purple_5;
            case "PURPLE6":
                return R.drawable.purple_6;
            case "YELLOW1":
                return R.drawable.yellow_1;
            case "YELLOW2":
                return R.drawable.yellow_2;
            case "YELLOW3":
                return R.drawable.yellow_3;
            case "YELLOW4":
                return R.drawable.yellow_4;
            case "YELLOW5":
                return R.drawable.yellow_5;
            case "YELLOW6":
                return R.drawable.yellow_6;

            default:
                return R.drawable.empty;
        }

    }
}
