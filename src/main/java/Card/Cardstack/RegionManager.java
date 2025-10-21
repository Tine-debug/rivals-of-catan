package Card.Cardstack;

import Card.Card;

import java.util.*;

public class RegionManager {
    private List<Card> regions = new ArrayList<>();

    public void initializeRegions(List<Card> cards) {
        regions = CardUtils.extractBy(cards, "type", "Region");
    }

    public void shuffle() {
        Collections.shuffle(regions);
    }

    public Card draw() {
        return regions.isEmpty() ? null : regions.remove(0);
    }

    public int size() {
        return regions.size();
    }

    public String print() {
        String regionsnames = "";
        for (Card card : regions) {
            regionsnames += card.toString() + "\n";
        }
        return regionsnames;
    }

    public Card pickByNameOrIndex(String spec) {
        if (spec == null || spec.isBlank()) return null;
        try {
            int idx = Integer.parseInt(spec.trim());
            return idx >= 0 && idx < regions.size() ? regions.remove(idx) : null;
        } catch (NumberFormatException ignored) {
        }
        return CardUtils.popCardByName(regions, spec);
    }

    public Card findUndiced(String name) {
        return regions.stream()
                .filter(c -> name.equalsIgnoreCase(c.getName()) && c.getdiceRoll() == 0)
                .findFirst().orElse(null);
    }

    public List<Card> getRegions(){
        return regions;
    }

}
