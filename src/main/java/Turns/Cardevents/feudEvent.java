package Turns.Cardevents;

import Player.Player;
import Player.Broadcast;
import Card.Cardstack.CardstackFacade;
import Card.Card;

public class feudEvent implements CardEvent {

    private final CardstackFacade stacks = CardstackFacade.getInstance();
    private final Broadcast broadcast = Broadcast.getInstance();

    @Override
    public void resolve(Player active, Player other) {
        Player adv = active.hasStrengthAdvantage(other) ? active
                : other.hasStrengthAdvantage(active) ? other
                : null;

        if (adv == null) {
            broadcast.broadcast("Feud: no strength advantage; nothing happens.");
            return;
        }

        Player opp = (adv == active) ? other : active;

        java.util.List<int[]> buildings = new java.util.ArrayList<>();
        for (int r = 0; r < opp.getPrincipality().principality.size(); r++) {
            var row = opp.getPrincipality().principality.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x != null && x.getType() != null && x.getType().equalsIgnoreCase("Building")) {
                    buildings.add(new int[]{r, c});
                }
            }
        }
        if (buildings.isEmpty()) {
            broadcast.broadcast("Feud: opponent has no buildings.");
            return;
        }
        adv.sendMessage(
                "PROMPT: Feud - select up to 3 opponent building coordinates as 'r c;r c;r c'. Opponent board:\n"
                + opp.printPrincipality());
        String line = adv.receiveMessage();
        java.util.List<int[]> picked = new java.util.ArrayList<>();
        try {
            for (String pair : line.split(";")) {
                String s = pair.trim();
                if (s.isEmpty()) {
                    continue;
                }
                String[] rc = s.split("\\s+");
                int r = Integer.parseInt(rc[0]);
                int c = Integer.parseInt(rc[1]);
                // validate it's a building
                Card x = opp.getCard(r, c);
                if (x != null && x.getType() != null && x.getType().equalsIgnoreCase("Building")) {
                    picked.add(new int[]{r, c});
                    if (picked.size() == 3) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        int k = 0;
        while (picked.size() < 3 && k < buildings.size()) {
            int[] bc = buildings.get(k++);
            boolean dup = false;
            for (int[] pc : picked) {
                if (pc[0] == bc[0] && pc[1] == bc[1]) {
                    dup = true;
                    break;
                }
            }
            if (!dup) {
                picked.add(bc);
            }
        }
        if (picked.isEmpty()) {
            broadcast.broadcast("Feud: no valid targets selected/found.");
            return;
        }

        StringBuilder opts = new StringBuilder("PROMPT: Feud - choose which to remove (index 0..")
                .append(picked.size() - 1)
                .append("):\n");
        for (int i = 0; i < picked.size(); i++) {
            int r = picked.get(i)[0], c = picked.get(i)[1];
            Card x = opp.getCard(r, c);
            opts.append("  [").append(i).append("] (").append(r).append(",").append(c).append(") ").append(x)
                    .append("\n");
        }
        opp.sendMessage(opts.toString());
        int choice = 0;
        try {
            choice = Integer.parseInt(opp.receiveMessage().trim());
        } catch (NumberFormatException ignored) {
        }
        if (choice < 0 || choice >= picked.size()) {
            choice = 0;
        }
        int rr = picked.get(choice)[0], cc = picked.get(choice)[1];
        Card removed = opp.getPrincipality().principality.get(rr).set(cc, null);
        broadcast.broadcast("Feud: removed " + (removed == null ? "unknown" : removed.toString()) + " from opponent at (" + rr + ","
                + cc + ").");
        stacks.placeCardBottomStack(removed, 1);
    }

}
