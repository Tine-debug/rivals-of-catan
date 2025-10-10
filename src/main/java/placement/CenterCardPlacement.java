
public class CenterCardPlacement extends Placement{

    public CenterCardPlacement() {
        this.placement = "Center Card";
    }
    
    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card){

        String nm = (card.name == null ? "" : card.name);
        System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");

        
             if (card.name.equals("City")) {
               return card.place_city(row, col, card, active);
            }

        if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
        }

            // 1) Center cards: Road / Settlement / City
        if (card.name.equals("Road") || card.name.equals("Settlement") || card.name.equals("City")) {
            if (row != 2) {
                active.sendMessage("Roads/Settlements/Cities must go in the center row(s).");
                return false;
            }

            if (card.name.equals("Road")) {
                // Just allow anywhere center that touches a center card or extends line
                // (Keep it permissive/ugly)
                active.placeCard(row, col, card);
                active.sendMessage("Built a Road.");
                // Expand board if we built at an edge
                active.expandAfterEdgeBuild(col);
                return true;
            }

            if (card.name.equals("Settlement")) {
                // Simplified: must be next to a Road (left or right) and empty here
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
        return false;
    }


}