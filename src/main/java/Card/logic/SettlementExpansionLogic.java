package Card.logic;

import Player.Player;
import Card.Card;
import Points.Points;

public class SettlementExpansionLogic implements Logic {

    public SettlementExpansionLogic() {
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card) {
        String nm = (card.getName() == null ? "" : card.getName());

        if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
        }

        if (is_Valid_placement_extentions(active, row, col, nm, card.getOneOf())) {
            return false;
        }
        System.out.println("Passed placement check");

        if ("Building".equalsIgnoreCase(card.getType())) {
            place_building(row, col, card, active);
            return true;
        }

        // Units
        if (card.getType().contains("Unit")) {
            System.out.println("Contained UNIT!!!!!");
            // Large Trade Ship: adjacency 2-for-1 between L/R regions (handled in Server)
            if (card.getName().equals("Large Trade Ship")) {
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
            active.points = Points.addPoints(active.points, card.getPoints());
            active.placeCard(row, col, card);
            return true;
        }
        return false;

    }

    private boolean is_Valid_placement_extentions(Player active, int row, int col, String nm, boolean oneOf) {
        if (!isAboveOrBelowSettlementOrCity(active, row, col)) {
            active.sendMessage("Expansion must be above/below a Settlement or City (fill inner ring first).");
            return true;
        }
        // one-of check (simple)
        if (oneOf) {
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
        if (nmAt(up1, "Settlement", "City")) {
            return true;
        }
        if (nmAt(down1, "Settlement", "City")) {
            return true;
        }

        // Outer ring allowed *only* if the inner slot is already filled (fill inner
        // first)
        Card up2 = p.getCard(row - 2, col);
        Card down2 = p.getCard(row + 2, col);
        boolean outerOK = ((nmAt(up2, "City", "City") || nmAt(up2, "Settlement", "Settlement")) && up1 != null)
                || ((nmAt(down2, "City", "City") || nmAt(down2, "Settlement", "Settlement")) && down1 != null);

        return outerOK;
    }

    private boolean nmAt(Card c, String a, String b) {
        if (c == null || c.getName() == null) {
            return false;
        }
        String n = c.getName();
        return n.equalsIgnoreCase(a) || n.equalsIgnoreCase(b);
    }

    private void place_building(int row, int col, Card card, Player player) {
        player.placeCard(row, col, card);
        System.out.println("Contained Building");
        switch (card.getName()) {
            case "Abbey":
                player.points.progressPoints += 1;
                break;
            case "Marketplace":
                player.flags.add("MARKETPLACE");
                break;
            case "Parish Hall":
                player.flags.add("PARISH");
                break;
            case "Storehouse":
                player.flags.add("STOREHOUSE@" + row + "," + col);
                break;
            case "Toll Bridge":
                player.flags.add("TOLLB");
                break;
            default:
                break;
        }

    }

}
