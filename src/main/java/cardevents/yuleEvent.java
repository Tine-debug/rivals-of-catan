
public class yuleEvent implements CardEvent {

    private Cardstacks stacks = Cardstacks.getInstance();

    @Override
    public void resolve(Player active, Player other) {
                stacks.resetEventstack();
                Card top = stacks.drawEventCard();
                if (top == null) {
                    broadcast("Event deck empty.", active, other);
                    return;
                }
                broadcast("EVENT: " + (top.cardText != null ? top.cardText : top.name), active, other);

                String nm = (top.name == null ? "" : top.name).toLowerCase();

                CardEvent event = CardEventFactory.createCardEvent(nm);
                event.resolve(active, other);
                
    }

    private void broadcast(String s, Player active, Player other) {
        active.sendMessage(s);
        other.sendMessage(s);
    }

}
