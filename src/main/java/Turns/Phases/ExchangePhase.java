package Turns.Phases;

import Card.Cardstack.CardstackFacade;
import Player.Player;
import Player.Broadcast;
import Card.Card;

public class ExchangePhase {

    private final static Broadcast broadcast = Broadcast.getInstance();
    private final static CardstackFacade stacks = CardstackFacade.getInstance();

    public static void resolvePhase(Player p) {
        int limit = 3 + p.getPoints().progressPoints;
        if (p.handSize() < limit) {
            broadcast.broadcast("Exchange: hand below limit; skipping.");
            return;
        }

        p.sendMessage("PROMPT: Exchange a card? (Y/N)");
        String ans = p.receiveMessage();
        if (ans == null || !ans.trim().toUpperCase().startsWith("Y")) {
            return;
        }

        p.sendMessage("PROMPT: Enter card name to put under a stack:");
        String nm = p.receiveMessage();
        Card chosen = p.removeFromHandByName(nm);
        if (chosen == null) {
            p.sendMessage("Not in hand.");
            return;
        }

        p.sendMessage("PROMPT: Choose stack [1-4] to put it under:");
        int st = readInt(p.receiveMessage(), 1);
        stacks.placeCardBottomStack(chosen, st);

        boolean hasParish = p.getFlags().contains("PARISH");
        int searchCost = hasParish ? 1 : 2;

        p.sendMessage("PROMPT: Choose Random draw (R) or Search (S, costs " + searchCost + " any)?");
        String mode = p.receiveMessage();
        if (mode != null && mode.trim().toUpperCase().startsWith("S")) {
            // Pay 1 (with Parish) or 2 (normal) resources of the player's choice
            if (p.totalAllResources() < searchCost) {
                p.sendMessage("Not enough resources to search.");
                return;
            }
            for (int i = 0; i < searchCost; i++) {
                p.sendMessage("PROMPT: Discard resource #" + (i + 1) + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                p.removeResource(p.receiveMessage(), 1);
            }
            stacks.chooseCardFromStack(st, p);

        } else {
            // Random draw (top of chosen stack)
            stacks.drawCard(st, p);
        }
    }

    private static int readInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
