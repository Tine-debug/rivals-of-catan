
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
        broadcast.broadcast("EVENT: " + (top.cardText != null ? top.cardText : top.name));

        String nm = (top.name == null ? "" : top.name).toLowerCase();

        CardEvent event = CardEventFactory.createCardEvent(nm);
        event.resolve(active, other);

    }
}
