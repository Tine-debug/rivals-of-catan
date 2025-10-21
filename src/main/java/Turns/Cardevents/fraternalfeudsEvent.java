package Turns.Cardevents;

import Player.Player;
import Player.Broadcast;
import Card.Cardstacks;
import Card.Card;


public class fraternalfeudsEvent implements CardEvent {

    private final Cardstacks stacks = Cardstacks.getInstance();
    private final Broadcast broadcast = Broadcast.getInstance();

    @Override
    public void resolve(Player other, Player active) {
        Player adv = active.hasStrengthAdvantage(other) ? active
                : other.hasStrengthAdvantage(active) ? other
                : null;

        if (adv == null) {
            broadcast.broadcast("Fraternal Feuds: no strength advantage; nothing happens.");
            return;
        }
        Player opp = (adv == active) ? other : active;

        if (opp.hand.isEmpty()) {
            broadcast.broadcast("Fraternal Feuds: opponent hand empty.");
            return;
        }

        adv.sendMessage("PROMPT: Opponent hand:\n" + opp.printHand() + "Choose up to two indices (e.g., '2 5'):");
        String sel = adv.receiveMessage();
        java.util.Set<Integer> idxs = new java.util.HashSet<>();
        try {
            for (String tok : sel.trim().split("\\s+")) {
                int i = Integer.parseInt(tok);
                if (i >= 0 && i < opp.hand.size()) {
                    idxs.add(i);
                }
                if (idxs.size() == 2) {
                    break;
                }
            }
        } catch (NumberFormatException ignored) {
        }

        // If insufficient/invalid, take first one or two
        if (idxs.isEmpty()) {
            idxs.add(0);
            if (opp.hand.handSize() > 1) {
                idxs.add(1);
            }
        }

        // Remove in descending order so indices stay valid
        java.util.List<Integer> order = new java.util.ArrayList<>(idxs);
        java.util.Collections.sort(order, java.util.Collections.reverseOrder());
        for (int i : order) {
            Card rem = opp.hand.removeCard(i);
            stacks.placeCardBottomStack(rem, 1);
            broadcast.broadcast("Fraternal Feuds: returned '" + rem.toString() + "' to bottom of a draw stack.");
        }

        markSkipReplenishOnce(opp);
        broadcast.broadcast("Fraternal Feuds: opponent cannot replenish hand at the end of the next turn.");
    }

    private void markSkipReplenishOnce(Player p) {
        if (p.flags == null) {
            p.flags = new java.util.HashSet<>();
        }
        p.flags.add("NO_REPLENISH_ONCE");
    }
}
