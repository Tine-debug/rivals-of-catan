


public class Placement{
    public String placement;

    public Placement(String placement) {
        this.placement = placement;
    }

    

   public boolean applyEffect(Player active, Player other, int row, int col, Card card) {
        String nm = (card.name == null ? "" : card.name);
        System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");



        if (card.name.equals("City")) {
               return card.place_city(row, col, card, active);
            }
        // 0) Early validation for occupied slot
        if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
        }

        // 1) Center cards: Road / Settlement / City
        if (card.name.equals("Road") || card.name.equals("Settlement") || card.name.equals("City")) {
            if (row != 2) {
                active.sendMessage("Roads/Settlements/Cities must go in the center row(s).");
                return false;
            }

            if (card.name.equals("Road")) {
                // Just allow anywhere center that touches a center card or extends line
                // (Keep it permissive/ugly)
                active.placeCard(row, col, card);
                active.sendMessage("Built a Road.");
                // Expand board if we built at an edge
                active.expandAfterEdgeBuild(col);
                return true;
            }

            if (card.name.equals("Settlement")) {
                // Simplified: must be next to a Road (left or right) and empty here
                Card L1 = active.getCard(row, col - 1);
                Card R1 = active.getCard(row, col + 1);
                boolean hasRoad = (L1 != null && L1.name.equals("Road"))
                        || (R1 != null && R1.name.equals("Road"));
                if (!hasRoad) {
                    active.sendMessage("Settlement must be placed next to a Road.");
                    return false;
                }
                active.placeCard(row, col, card);
                active.victoryPoints += 1;

                // Expand and capture the updated column
                col = active.expandAfterEdgeBuild(col);

                // Now place diagonals using the correct, updated col
                card.placeTwoDiagonalRegions(active, row, col);

                active.lastSettlementRow = row;
                active.lastSettlementCol = col;
                return true;
            }

            
        }

        // 2) Regions: allow only in region rows (not center); we set default
        // production=1
        if ("Region".equalsIgnoreCase(this.placement)) {
            if (row == 2) {
                active.sendMessage("Regions must be placed above/below the center row.");
                return false;
            }
            if (card.regionProduction <= 0)
                card.regionProduction = 1;
            active.placeCard(row, col, card);
            return true;
        }


        // 3) Settlement/City Expansions (Buildings & Units)
        if (placement != null && placement.equalsIgnoreCase("Settlement/city")) {

            if (card.is_Valid_placement_extentions(active, row, col, nm)) return false;
            System.out.println("Passed placement checks for " + nm);

            if ("Building".equalsIgnoreCase(card.type)) {
                card.place_building(row, col, card, active);
                return true;
            }

            // Units
            if (card.type.contains("Unit")) {
                System.out.println("Contained UNIT!!!!!");
                // Large Trade Ship: adjacency 2-for-1 between L/R regions (handled in Server)
                if (card.name.equals("Large Trade Ship")) {
                    active.placeCard(row, col, card);
                    active.flags.add("LTS@" + row + "," + col);
                    return true;
                }
                // “Common” trade ships: 2:1 bank for specific resource (handled in Server)
                if (nm.toLowerCase().endsWith(" ship")) {
                    active.placeCard(row, col, card);
                    String res = nm.split("\\s+")[0]; // Brick/Gold/Grain/Lumber/Ore/Wool
                    active.flags.add("2FOR1_" + res.toUpperCase());
                    return true;
                }

                // Heroes: just add SP/FP/CP/etc.
                int sp = Card.asInt(card.SP, 0), fp = Card.asInt(card.FP, 0), cp = Card.asInt(card.CP, 0), pp = Card.asInt(card.PP, 0), kp = Card.asInt(card.KP, 0);
                active.skillPoints += fp;
                active.strengthPoints += sp;
                active.commercePoints += cp;
                active.progressPoints += pp;
                active.placeCard(row, col, card);
                return true;
            }
        }
        
        // 4) Pure action cards (Basic intro handful):
        // We keep these very small; most are handled in Server (events/Brigand etc.)
        if ("Action".equalsIgnoreCase(placement) || "Action".equalsIgnoreCase(card.type)) {
            // e.g., Merchant Caravan: “gain 2 of your choice by discarding any 2 resources”
            if (card.name.equals("Merchant Caravan")) {
                if (active.totalAllResources() < 2) {
                    active.sendMessage("You need at least 2 resources to play Merchant Caravan.");
                    return false;
                }
                // Just let player pick 2 to discard, then 2 to gain
                // (opting out by discarding and gaining same resource is allowed)
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
                // Only meaningful when used with a new settlement (Server stores
                // lastSettlementRow/Col)
                active.flags.add("SCOUT_NEXT_SETTLEMENT");
                return true;
            }

            if (card.name.equals("Brigitta, the Wise Woman")) {
                // Choose production die result before rolling; we store forced value in Server
                active.flags.add("BRIGITTA");
                return true;
            }

            // Discard 3 gold and take any 2 resources of your choice in return.
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

            // Swap 2 of your own Regions OR 2 of your own Expansion cards.
            // Stored resources on Regions remain on the same cards; placement rules must
            // hold.
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

                // Read two coordinates
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
                    // target slots must be valid region slots (i.e., not center)
                    if (r1 == 2 || r2 == 2) {
                        active.sendMessage("Relocation: regions must be outside center row.");
                        return false;
                    }
                    // Swap without re-applying effects
                    active.placeCard(r1, c1, b);
                    active.placeCard(r2, c2, a);
                    active.sendMessage("Relocation done (Regions swapped).");
                    return true;
                } else { // swapExp
                    if (!card.isExpansionCard(a) || !card.isExpansionCard(b)) {
                        active.sendMessage("Relocation (Expansion): both cards must be expansions.");
                        return false;
                    }
                    // Must still obey expansion placement for each target slot
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

            // Default: treat as “+1 VP”
            active.victoryPoints += 1;
            active.sendMessage("Played " + nm + ": +1 VP (default).");
            return true;
        }

        // Fallback: accept placement (ugly default)
        active.placeCard(row, col, card);
        return true;
    }

    



}