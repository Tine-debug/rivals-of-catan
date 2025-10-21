package Card.Cardstack;

import Card.Card;
import Player.Player;
import java.util.*;

public class CenterBuildingManager {

    private List<Card> roads = new ArrayList<>();
    private List<Card> settlements = new ArrayList<>();
    private List<Card> cities = new ArrayList<>();


    public void initializeCenterCards(List<Card> allCards) {
        roads = CardUtils.extractBy(allCards, "name", "Road");
        settlements = CardUtils.extractBy(allCards, "name", "Settlement");
        cities = CardUtils.extractBy(allCards, "name", "City");
    }

    public List<String> getBuildingCosts() {
        List<String> costs = new ArrayList<>();
        if (!roads.isEmpty()) costs.add("ROAD(" + getCost(roads.get(0)) + ")");
        if (!settlements.isEmpty()) costs.add("SETTLEMENT(" + getCost(settlements.get(0)) + ")");
        if (!cities.isEmpty()) costs.add("CITY(" + getCost(cities.get(0)) + ")");
        return costs;
    }

    private String getCost(Card card) {
        return card.getCost() == null ? "-" : card.getCost();
    }

    public List<Card> getStack(String Name){
        switch (Name) {
            case "Road": return roads;
            case "Settlement": return settlements;
            case "City": return cities;
            default:
                return null;
        }
    }

    public int[] placeCenterCard(Player active, Player other, String type) {
 List<Card> pile = null;
        if (type.equalsIgnoreCase("Road")) {
            pile = roads;
        } else if (type.equalsIgnoreCase("Settlement")) {
            pile = settlements;
        } else if (type.equalsIgnoreCase("City")) {
            pile = cities;
        }

        if (pile == null || pile.isEmpty()) {
            active.sendMessage("No " + type + " cards left in the pile.");
            return new int[]{-1, -1};
        }

        Card proto = pile.get(0);

        if (!active.payCost(proto.getCost())) {
            active.sendMessage("Can't afford cost: " + (proto.getCost() == null ? "-" : proto.getCost()));
            return new int[]{-1, -1};
        }

        active.sendMessage("PROMPT: Enter placement coordinates as: ROW COL");
        int row, col;
        try {
            String[] rc = active.receiveMessage().trim().split("\\s+");
            row = Integer.parseInt(rc[0]);
            col = Integer.parseInt(rc[1]);
        } catch (NumberFormatException e) {
            active.sendMessage("Invalid coordinates. Use: ROW COL (e.g., 2 3)");
            active.refundCost(proto.getCost());
            return new int[]{-1, -1};
        }

        boolean ok = proto.applyEffect(active, other, row, col);
        if (!ok) {
            active.sendMessage("Illegal placement/effect; refunding cost.");
            active.refundCost(proto.getCost());
            return new int[]{-1, -1};
        }

        pile.remove(0);
        return new int[]{row, col};

    }


    public void inizializePrincipiality(Player p, int[][] dice, int center, int i, List<Card> regions) {
        p.placeCard(center, 1, CardUtils.popCardByName(settlements, "Settlement"));
        p.placeCard(center, 2, CardUtils.popCardByName(roads, "Road"));
        p.placeCard(center, 3, CardUtils.popCardByName(settlements, "Settlement"));

        String[] names = {"Forest", "Gold Field", "Field", "Hill", "Pasture", "Mountain"};
        for (int j = 0; j < names.length; j++) {
            Card card = CardUtils.popCardByName(regions, names[j]);
            if (card != null) {
                card.setDiceRoll(dice[i][j]);
                card.setRegionProduction(names[j].equals("Gold Field") ? 0 : 1);
            }
            int row = j < 3 ? center - 1 : center + 1;
            int col = (j % 3) * 2;
            p.placeCard(row, col, card);
        }
    }
}
