

public class SettlementExpansionLogic implements Logic{
    public SettlementExpansionLogic(){
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card){
                String nm = (card.name == null ? "" : card.name);
            

            if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
            }


            if (is_Valid_placement_extentions(active, row, col, nm, card.oneOf)) return false;
            System.out.println("Passed placement check");

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
                int sp = asInt(card.SP, 0), fp = asInt(card.FP, 0), cp = asInt(card.CP, 0), pp = asInt(card.PP, 0), kp = asInt(card.KP, 0);
                active.skillPoints += fp;
                active.strengthPoints += sp;
                active.commercePoints += cp;
                active.progressPoints += pp;
                active.placeCard(row, col, card);
                return true;
            }
    return false;

    }
    
    private int asInt(String s, int def) {
        try {
            if (s == null)
                return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private boolean is_Valid_placement_extentions(Player active, int row, int col, String nm, String oneOf) {
        if (!isAboveOrBelowSettlementOrCity(active, row, col)) {
            active.sendMessage("Expansion must be above/below a Settlement or City (fill inner ring first).");
            return true;
        }
        // one-of check (simple)
        if (oneOf != null && oneOf.trim().equalsIgnoreCase("1x")) {
            if (active.hasInPrincipality(nm)) {
                active.sendMessage("You may only have one '" + nm + "' in your principality.");
                return true;
            }
        }
        return false;
    }

    private boolean isAboveOrBelowSettlementOrCity(Player p, int row, int col) {
        // Inner ring: ±1 from center settlement/city
        Card up1 = p.getCard(row - 1, col);
        Card down1 = p.getCard(row + 1, col);
        if (nmAt(up1, "Settlement", "City"))
            return true;
        if (nmAt(down1, "Settlement", "City"))
            return true;

        // Outer ring allowed *only* if the inner slot is already filled (fill inner
        // first)
        Card up2 = p.getCard(row - 2, col);
        Card down2 = p.getCard(row + 2, col);
        boolean outerOK = ((nmAt(up2, "City", "City") || nmAt(up2, "Settlement", "Settlement")) && up1 != null) ||
                ((nmAt(down2, "City", "City") || nmAt(down2, "Settlement", "Settlement")) && down1 != null);

        return outerOK;
    }
    
    private boolean nmAt(Card c, String a, String b) {
        if (c == null || c.name == null)
            return false;
        String n = c.name;
        return n.equalsIgnoreCase(a) || n.equalsIgnoreCase(b);
    }

    


}