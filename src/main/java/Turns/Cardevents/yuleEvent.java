package Turns.Cardevents;

import Player.Player;
import Card.Cardstacks;
import Card.Card;
import Player.Broadcast;


public class yuleEvent implements CardEvent {

    private final Cardstacks stacks = Cardstacks.getInstance();
    private final Broadcast broadcast = Broadcast.getInstance();

    @Override
    public void resolve(Player active, Player other) {
        stacks.resetEventstack();
        Card top = stacks.drawEventCard();
        if (top == null) {
            broadcast.broadcast("Event deck empty.");
            return;
        }
        broadcast.broadcast("EVENT: " + (top.getCardText() != null ? top.getCardText() : top.toString()));

        String nm = (top.toString() == null ? "" : top.toString()).toLowerCase();

        CardEvent event = CardEventFactory.createCardEvent(nm);
        event.resolve(active, other);

    }
}
