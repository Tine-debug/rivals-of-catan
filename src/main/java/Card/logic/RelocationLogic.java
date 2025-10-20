package Card.logic;

import Player.Player;
import Card.Card;

public class RelocationLogic implements Logic {

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public boolean applyEffect(Player active, Player other, int row, int col, Card card) {
        active.sendMessage(
                "PROMPT: Relocation - Type 'REGION' to swap two regions or 'EXP' to swap two expansions:");
        String pick = active.receiveMessage();
        boolean swapRegions = (pick != null && pick.trim().toUpperCase().startsWith("R"));
        boolean swapExp = (pick != null && pick.trim().toUpperCase().startsWith("E"));
        if (!swapRegions && !swapExp) {
            active.sendMessage("Relocation canceled (need REGION or EXP).");
            return false;
        }

        active.sendMessage("PROMPT: Enter first coordinate (row col):");
        int r1, c1 ;
        try {
            String[] t = active.receiveMessage().trim().split("\\s+");
            r1 = Integer.parseInt(t[0]);
            c1 = Integer.parseInt(t[1]);
        } catch (Exception e) {
            active.sendMessage("Invalid coordinate.");
            return false;
        }
        active.sendMessage("PROMPT: Enter second coordinate (row col):");
        int r2, c2;
        try {
            String[] t = active.receiveMessage().trim().split("\\s+");
            r2 = Integer.parseInt(t[0]);
            c2 = Integer.parseInt(t[1]);
        } catch (Exception e) {
            active.sendMessage("Invalid coordinate.");
            return false;
        }

        Card a = active.getCard(r1, c1);
        Card b = active.getCard(r2, c2);
        if (a == null || b == null) {
            active.sendMessage("Relocation: both positions must contain cards.");
            return false;
        }

        if (swapRegions) {
            if (!isRegionCard(a) || !isRegionCard(b)) {
                active.sendMessage("Relocation (Region): both cards must be Regions.");
                return false;
            }

            if (r1 == 2 || r2 == 2) {
                active.sendMessage("Relocation: regions must be outside center row.");
                return false;
            }

            active.placeCard(r1, c1, b);
            active.placeCard(r2, c2, a);
            active.sendMessage("Relocation done (Regions swapped).");
            return true;
        } else {
            if (!isExpansionCard(a) || !isExpansionCard(b)) {
                active.sendMessage("Relocation (Expansion): both cards must be expansions.");
                return false;
            }
            if (!isAboveOrBelowSettlementOrCity(active, r2, c2)
                    || !isAboveOrBelowSettlementOrCity(active, r1, c1)) {
                active.sendMessage("Relocation: target slot is not valid for an expansion.");
                return false;
            }
            active.placeCard(r1, c1, b);
            active.placeCard(r2, c2, a);
            active.sendMessage("Relocation done (Expansions swapped).");
            return true;
        }
    }

    private boolean isRegionCard(Card c) {
        return c != null && c.type != null && c.type.equalsIgnoreCase("Region");
    }

    private boolean isExpansionCard(Card c) {
        if (c == null) {
            return false;
        }
        String pl = (c.placement == null ? "" : c.placement.toLowerCase());
        return pl.contains("settlement/city");
    }

    private boolean isAboveOrBelowSettlementOrCity(Player p, int row, int col) {

        Card up1 = p.getCard(row - 1, col);
        Card down1 = p.getCard(row + 1, col);
        if (nmAt(up1, "Settlement", "City")) {
            return true;
        }
        if (nmAt(down1, "Settlement", "City")) {
            return true;
        }

        Card up2 = p.getCard(row - 2, col);
        Card down2 = p.getCard(row + 2, col);
        boolean outerOK = ((nmAt(up2, "City", "City") || nmAt(up2, "Settlement", "Settlement")) && up1 != null)
                || ((nmAt(down2, "City", "City") || nmAt(down2, "Settlement", "Settlement")) && down1 != null);

        return outerOK;
    }

    private boolean nmAt(Card c, String a, String b) {
        if (c == null || c.name == null) {
            return false;
        }
        String n = c.name;
        return n.equalsIgnoreCase(a) || n.equalsIgnoreCase(b);
    }

}
