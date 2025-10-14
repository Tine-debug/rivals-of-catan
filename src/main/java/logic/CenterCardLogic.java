
public class CenterCardLogic extends Logic{

    public CenterCardLogic() {}
    
    
    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card){

        String nm = (card.name == null ? "" : card.name);
        System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");

        if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
        }

            // 1) Center cards: Road / Settlement / City
        if (card.name.equals("Road")) {
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

            
        }
        return false;
    }


}