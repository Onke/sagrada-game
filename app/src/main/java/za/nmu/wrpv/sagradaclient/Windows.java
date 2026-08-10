package za.nmu.wrpv.sagradaclient;

import java.util.List;

import nmu.sagrada.core.Cell;
import nmu.sagrada.core.Die;
import nmu.sagrada.core.PlacementValidator;
import nmu.sagrada.core.WindowCard;
import nmu.sagrada.core.WindowPatterns;

/**
 * Rebuilds a playable {@link WindowCard} from the flat data the server sends, so the client can
 * draw real restrictions, preview legal moves and project a score without waiting for a round trip.
 */
final class Windows {

    private static final PlacementValidator VALIDATOR = new PlacementValidator();

    private Windows() {}

    /** Returns null when the player has not chosen a window yet. */
    static WindowCard build(String windowId, List<String> dicePositions) {
        if (windowId == null || windowId.isEmpty()) return null;
        WindowCard window;
        try {
            window = new WindowCard(WindowPatterns.patternFor(windowId));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        if (dicePositions == null) return window;
        int limit = Math.min(dicePositions.size(), WindowCard.SIZE);
        for (int i = 0; i < limit; i++) {
            String id = dicePositions.get(i);
            if (!Dice.isDie(id)) continue;
            try {
                window.place(i, Die.fromId(id));
            } catch (RuntimeException ignored) {
                // A malformed or duplicated id only costs us one drawn die; keep the rest.
            }
        }
        return window;
    }

    static WindowCard pattern(String windowId) {
        return build(windowId, null);
    }

    /** Cells where {@code die} may legally go. Advisory only — the server still decides. */
    static boolean[] legalCells(WindowCard window, String dieId) {
        boolean[] legal = new boolean[WindowCard.SIZE];
        if (window == null || !Dice.isDie(dieId)) return legal;
        Die die;
        try {
            die = Die.fromId(dieId);
        } catch (RuntimeException ignored) {
            return legal;
        }
        for (int i = 0; i < WindowCard.SIZE; i++) {
            legal[i] = VALIDATOR.validate(window, die, i).isLegal();
        }
        return legal;
    }

    static boolean hasAny(boolean[] flags) {
        for (boolean flag : flags) if (flag) return true;
        return false;
    }

    /** True when no die in the pool fits anywhere — the player is genuinely stuck and must pass. */
    static boolean stuck(WindowCard window, List<String> draftPool) {
        if (window == null || draftPool == null) return false;
        for (String dieId : draftPool) {
            if (hasAny(legalCells(window, dieId))) return false;
        }
        return true;
    }

    static int countColourCells(WindowCard window) {
        int count = 0;
        for (Cell cell : window.getCells()) if (cell.getColourRestriction() != null) count++;
        return count;
    }

    static int countValueCells(WindowCard window) {
        int count = 0;
        for (Cell cell : window.getCells()) if (cell.getValueRestriction() != 0) count++;
        return count;
    }

    static int countOpenCells(WindowCard window) {
        return WindowCard.SIZE - countColourCells(window) - countValueCells(window);
    }
}
