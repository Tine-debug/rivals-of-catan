public class SettlementLogic implements Logic{

    @Override
     public boolean applyEffect(Player active, Player other, int row, int col, Card card){

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
                boolean hasRoad = (L1 != null && L1.name.equals("Road"))
                        || (R1 != null && R1.name.equals("Road"));
                if (!hasRoad) {
                    active.sendMessage("Settlement must be placed next to a Road.");
                    return false;
                }
                active.placeCard(row, col, card);
                active.victoryPoints += 1;

                // Expand and capture the updated column
                col = active.expandAfterEdgeBuild(col);

                // Now place diagonals using the correct, updated col
                card.placeTwoDiagonalRegions(active, row, col);

                active.lastSettlementRow = row;
                active.lastSettlementCol = col;
                return true;


    }



}