
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Hand {

    public List<Card> hand = new ArrayList<>();

    public String printHand() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hand (").append(hand.size()).append("):\n");
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c == null) {
                continue;
            }
            String cost = (c.cost == null || c.cost.isBlank()) ? "-" : c.cost;
            String pts = c.summarizePoints();
            sb.append("  [").append(i).append("] ")
                    .append(c.name == null ? "Unknown" : c.name)
                    .append("   {cost: ").append(cost).append("} ")
                    .append(pts.isEmpty() ? "" : pts)
                    .append("\n").append(c.cardText == null ? "" : "\t" + c.cardText + "\n");
        }
        return sb.toString();
    }

    public Card removeFromHandByName(String nm) {
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c != null && c.name != null && c.name.equalsIgnoreCase(nm)) {
                return hand.remove(i);
            }
        }
        return null;
    }

    public Card findCardInHand(Player p, String spec) {
        if (spec == null) {
            return null;
        }
        spec = spec.trim();

        try {
            int idx = Integer.parseInt(spec);
            if (idx >= 0 && idx < hand.size()) {
                return hand.get(idx);
            }
        } catch (NumberFormatException ignored) {
        }

        for (Card c : hand) {
            if (c != null && c.name != null && c.name.equalsIgnoreCase(spec)) {
                return c;
            }
        }

        String lower = spec.toLowerCase();
        for (Card c : hand) {
            if (c != null && c.name != null && c.name.toLowerCase().startsWith(lower)) {
                return c;
            }
        }
        return null;
    }

    public void addToHand(Card c) {
        hand.add(c);
    }

    public int handSize() {
        return hand.size();
    }

}
