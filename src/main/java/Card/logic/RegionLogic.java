package Card.logic;

import Player.Player;
import Card.Card;

public class RegionLogic implements Logic {

    public RegionLogic() {
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card) {

        if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
        }
        if (row == 2 || row == 4 || row == 0) {
            active.sendMessage("Regions must be placed above/below the center row.");
            return false;
        }
        if (active.getCard(2, col) != null) {
            return false;
        }

        if (card.getRegionProduction() < 0) {
            card.setRegionProduction(0);
        }
        active.placeCard(row, col, card);
        return true;
    }

}
