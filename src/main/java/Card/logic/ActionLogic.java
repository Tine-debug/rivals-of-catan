
public class ActionLogic implements Logic {

    public ActionLogic() {
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card) {

        if (card.name.equals("Scout")) {
            active.flags.add("SCOUT_NEXT_SETTLEMENT");
            return true;
        }

        if (card.name.equals("Brigitta, the Wise Woman")) {
            active.flags.add("BRIGITTA");
            return true;
        }

        if (card.name.equals("Goldsmith")) {
            if (!active.removeResource("Gold", 3)) {
                active.sendMessage("Goldsmith: you need 3 Gold to play card.");
                return false;
            }
            active.sendMessage("Goldsmith: choose two resources to gain:");
            for (int i = 1; i <= 2; i++) {
                active.sendMessage("Pick resource #" + i + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                String g = active.receiveMessage();
                active.gainResource(g);
            }
            return true;
        }

        active.sendMessage("Card Logic not found");
        return false;
    }

}
