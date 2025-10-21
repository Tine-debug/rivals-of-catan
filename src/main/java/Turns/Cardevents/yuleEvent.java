package Turns.Cardevents;

import Player.Player;
import Card.Cardstack.CardstackFacade;
import Card.Card;
import Player.Broadcast;


public class yuleEvent implements CardEvent {

    private final CardstackFacade stacks = CardstackFacade.getInstance();
    private final Broadcast broadcast = Broadcast.getInstance();

    @Override
    public void resolve(Player active, Player other) {
        stacks.resetEventStack();
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
