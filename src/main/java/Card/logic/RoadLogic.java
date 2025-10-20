
public class RoadLogic implements Logic {

    public RoadLogic() {
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card) {

        if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
        }

        if (row != 2) {
            active.sendMessage("Roads must go in the center row(s).");
            return false;
        }
        active.placeCard(row, col, card);
        active.sendMessage("Built a Road.");
        active.expandAfterEdgeBuild(col);
        return true;
    }

}
