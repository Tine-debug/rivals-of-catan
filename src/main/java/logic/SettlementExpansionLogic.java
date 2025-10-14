

public class SettlementExpansionLogic extends Logic{
    public SettlementExpansionLogic(){
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card){

            String nm = (card.name == null ? "" : card.name);
            System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");

            if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
            }


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
    return false;

    }
    
}