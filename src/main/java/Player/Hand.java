package Player;

import java.util.ArrayList;
import java.util.List;
import Card.Card;

public class Hand {

    private List<Card> hand = new ArrayList<>();

    public void removeCard(Card card){
        hand.remove(card);
    }

    public Card removeCard(int i){
        return hand.remove(i);
    }

    public boolean isEmpty(){
        return hand.isEmpty();
    }

    public int size(){
        return hand.size();
    }

    public String printHand() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hand (").append(hand.size()).append("):\n");
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c == null) {
                continue;
            }
            String cost = (c.getCost() == null || c.getCost().isBlank()) ? "-" : c.getCost();
            String pts = c.summarizePoints();
            sb.append("  [").append(i).append("] ")
                    .append(c.toString())
                    .append("   {cost: ").append(cost).append("} ")
                    .append(pts.isEmpty() ? "" : pts)
                    .append("\n").append(c.getCardText() == null ? "" : "\t" + c.getCardText() + "\n");
        }
        return sb.toString();
    }

    public Card removeFromHandByName(String nm) {
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c != null && c.getName() != null && c.getName().equalsIgnoreCase(nm)) {
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
            if (c != null && c.getName() != null && c.getName().equalsIgnoreCase(spec)) {
                return c;
            }
        }

        String lower = spec.toLowerCase();
        for (Card c : hand) {
            if (c != null && c.getName() != null && c.getName().toLowerCase().startsWith(lower)) {
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
