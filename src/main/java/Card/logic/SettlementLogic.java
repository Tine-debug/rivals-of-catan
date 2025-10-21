package Card.logic;

import Player.Player;
import Card.Card;
import Card.Cardstacks;

public class SettlementLogic implements Logic {

    private final Cardstacks stacks = Cardstacks.getInstance();

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card) {

        if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
        }

        if (row != 2) {
            active.sendMessage("Settlements must go in the center row(s).");
            return false;
        }

        Card L1 = active.getCard(row, col - 1);
        Card R1 = active.getCard(row, col + 1);
        boolean hasRoad = (L1 != null && L1.getName().equals("Road"))
                || (R1 != null && R1.getName().equals("Road"));
        if (!hasRoad) {
            active.sendMessage("Settlement must be placed next to a Road.");
            return false;
        }
        active.placeCard(row, col, card);
        active.points.victoryPoints += 1;

        // Expand and capture the updated column
        col = active.expandAfterEdgeBuild(col);

        // Now place diagonals using the correct, updated col
        placeTwoDiagonalRegions(active, row, col);

        active.setLastSettlementRow(row);
        active.setLastSettlementCol(col);
        return true;

    }

    public void placeTwoDiagonalRegions(Player active, int row, int col) {
        // Decide which side is the “open side” (the side without a road)
        int colMod = (active.getCard(row, col - 1) == null) ? -1 : 1;
        int sideCol = col + colMod;

        // Draw or choose 2 regions
        Card first, second;

        if (active.getFlags().contains("SCOUT_NEXT_SETTLEMENT")) {
            // SCOUT: let player pick two specific regions from the region stack by name or
            // index
            active.sendMessage("PROMPT: SCOUT - Choose first region (name or index):");
            String s1 = active.receiveMessage();
            first = stacks.pickRegionFromStackByNameOrIndex(s1);
            if (first == null) {
                // fallback to top
                first = stacks.drawregionCard();
            }

            active.sendMessage("PROMPT: SCOUT - Choose second region (name or index):");
            String s2 = active.receiveMessage();
            second = stacks.pickRegionFromStackByNameOrIndex(s2);
            if (second == null) {
                second = stacks.drawregionCard();
            }

            if (first == null || second == null) {
                active.sendMessage("SCOUT: Region stack exhausted.");
                // still clear the flag to avoid leaking it
                active.getFlags().remove("SCOUT_NEXT_SETTLEMENT");
                return;
            }
        } else {
            // normal: take top two
            if (stacks.getRegionstackSize() < 2) {
                active.sendMessage("Region stack does not have two cards.");
                return;
            }
            first = stacks.drawregionCard();
            second = stacks.drawregionCard();
        }

        // Tell the player which two we drew/selected
        active.sendMessage("New settlement regions drawn/selected:");
        active.sendMessage("  1) " + first.toString() + "   2) " + second.toString());

        // Ask where to put the first one (top/bottom), second goes to the other
        active.sendMessage("PROMPT: Place FIRST region on " + (colMod == -1 ? "LEFT" : "RIGHT")
                + " side: TOP or BOTTOM? (T/B)");
        String choice = active.receiveMessage();
        boolean top = choice != null && choice.trim().toUpperCase().startsWith("T");
        row = 2; // center row
        int topRow = row - 1;
        int bottomRow = row + 1;

        if (top) {
            active.placeCard(topRow, sideCol, first);
            active.placeCard(bottomRow, sideCol, second);
        } else {
            active.placeCard(topRow, sideCol, second);
            active.placeCard(bottomRow, sideCol, first);
        }

        // SCOUT benefit is consumed now; clear the flag
        active.getFlags().remove("SCOUT_NEXT_SETTLEMENT");
    }

    // Helper: choose region by name or index from Cardstacks.regions
}
