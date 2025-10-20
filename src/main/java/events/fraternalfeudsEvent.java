
public class fraternalfeudsEvent implements Event {

    private Cardstacks stacks = Cardstacks.getInstance();

    @Override
    public void resolve(Player other, Player active) {
        Player adv = Server.hasStrengthAdvantage(active, other) ? active
                : Server.hasStrengthAdvantage(other, active) ? other
                : null;

        if (adv == null) {
            broadcast("Fraternal Feuds: no strength advantage; nothing happens.", active, other);
            return;
        }
        Player opp = (adv == active) ? other : active;

        if (opp.hand.hand.isEmpty()) {
            broadcast("Fraternal Feuds: opponent hand empty.", active, other);
            return;
        }

        adv.sendMessage("PROMPT: Opponent hand:\n" + opp.printHand() + "Choose up to two indices (e.g., '2 5'):");
        String sel = adv.receiveMessage();
        java.util.Set<Integer> idxs = new java.util.HashSet<>();
        try {
            for (String tok : sel.trim().split("\\s+")) {
                int i = Integer.parseInt(tok);
                if (i >= 0 && i < opp.hand.hand.size()) {
                    idxs.add(i);
                }
                if (idxs.size() == 2) {
                    break;
                }
            }
        } catch (Exception ignored) {
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
            Card rem = opp.hand.hand.remove(i);
            stacks.returnBuildingToBottom(rem, 1);
            broadcast("Fraternal Feuds: returned '" + rem.name + "' to bottom of a draw stack.", active, other);
        }

        markSkipReplenishOnce(opp);
        broadcast("Fraternal Feuds: opponent cannot replenish hand at the end of the next turn.", active, other);
    }

    private void broadcast(String s, Player active, Player other) {
        active.sendMessage(s);
        other.sendMessage(s);
    }

    private void markSkipReplenishOnce(Player p) {
        if (p.flags == null) {
            p.flags = new java.util.HashSet<>();
        }
        p.flags.add("NO_REPLENISH_ONCE");
    }
}
