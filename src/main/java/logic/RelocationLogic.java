public class RelocationLogic implements Logic {

@Override
public boolean applyEffect(Player active, Player other, int row, int col, Card card){
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
                int r1 = 0, c1 = 0;
                try {
                    String[] t = active.receiveMessage().trim().split("\\s+");
                    r1 = Integer.parseInt(t[0]);
                    c1 = Integer.parseInt(t[1]);
                } catch (Exception e) {
                    active.sendMessage("Invalid coordinate.");
                    return false;
                }
                active.sendMessage("PROMPT: Enter second coordinate (row col):");
                int r2 = 0, c2 = 0;
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
                    if (!Card.isAboveOrBelowSettlementOrCity(active, r2, c2)
                            || !Card.isAboveOrBelowSettlementOrCity(active, r1, c1)) {
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
        if (c == null)
            return false;
        String pl = (c.placement == null ? "" : c.placement.toLowerCase());
        return pl.contains("settlement/city");
    }

}