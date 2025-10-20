package Card.logic;

import Player.Player;
import Card.Card;

public class CityLogic implements Logic {

    public CityLogic() {
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card) {
        Card under = active.getCard(row, col);
        if (under == null || !(under.getName().equals("Settlement"))) {
            active.sendMessage("City must be placed on top of an existing Settlement (same slot).");
            return false;
        }
        active.placeCard(row, col, card);
        active.points.victoryPoints += 1;
        return true;
    }

}
