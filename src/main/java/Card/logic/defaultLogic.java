package Card.logic;

import Player.Player;
import Card.Card;

public class defaultLogic implements Logic {

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card) {

        active.sendMessage("No logic implemened");
        return false;

    }

}
