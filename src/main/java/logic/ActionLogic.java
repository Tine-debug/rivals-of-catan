
public class ActionLogic implements  Logic{
     public ActionLogic() {}



    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card){

            String nm = (card.name == null ? "" : card.name);
            System.out.println("ApplyEffect: " + nm);

            if (card.name.equals("Merchant Caravan")) {
                if (active.totalAllResources() < 2) {
                    active.sendMessage("You need at least 2 resources to play Merchant Caravan.");
                    return false;
                }
                for (int i = 0; i < 2; i++) {
                    active.sendMessage(
                            "PROMPT: Type Discard resource #" + (i + 1) + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    String g = active.receiveMessage();
                    active.removeResource(g, 1);
                }
                for (int i = 0; i < 2; i++) {
                    active.sendMessage(
                            "PROMPT: Type Gain resource #" + (i + 1) + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    String g = active.receiveMessage();
                    active.gainResource(g);
                }
                return true;
            }

            if (card.name.equals("Scout")) {
                active.flags.add("SCOUT_NEXT_SETTLEMENT");
                return true;
            }

            if (card.name.equals("Brigitta, the Wise Woman")) {
                active.flags.add("BRIGITTA");
                return true;
            }

            if (card.name.equals("Goldsmith")) {
                if (!active.removeResource("Gold", 3)) {
                    active.sendMessage("Goldsmith: you need 3 Gold to play card.");
                    return false;
                }
                active.sendMessage("Goldsmith: choose two resources to gain:");
                for (int i = 1; i <= 2; i++) {
                    active.sendMessage("Pick resource #" + i + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    String g = active.receiveMessage();
                    active.gainResource(g);
                }
                return true;
            }


            if (card.name.equals("Relocation")) {
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
                    if (!card.isRegionCard(a) || !card.isRegionCard(b)) {
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
                    if (!card.isExpansionCard(a) || !card.isExpansionCard(b)) {
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

            active.sendMessage("Card Logic not found");
            return false;
        }



}