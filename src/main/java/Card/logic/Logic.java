package Card.logic;

import Player.Player;
import Card.Card;

public interface Logic {

    public boolean applyEffect(Player active, Player other, int row, int col, Card card);
}
