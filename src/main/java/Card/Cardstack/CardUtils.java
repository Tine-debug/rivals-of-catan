package Card.Cardstack;

import java.util.ArrayList;
import java.util.List;

import Card.Card;

public class CardUtils {

    public static Card popCardByName(List<Card> cards, String name) {
        if (cards == null || name == null) {
            return null;
        }
        String target = name.trim();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            if (c != null && c.getName() != null && c.getName().trim().equalsIgnoreCase(target)) {
                return cards.remove(i);
            }
        }
        return null;
    }

    public static List<Card> extractBy(List<Card> cards, String attribute, String value) {
        List<Card> out = new ArrayList<>();
        for (int i = cards.size() - 1; i >= 0; i--) {
            Card c = cards.get(i);
            switch (attribute) {
                case "type":
                    if (c.getType().equals(value)) {
                        out.add(0, cards.remove(i));
                    }
                    break;
                case "name":
                    if (c.getName().equals(value)) {
                        out.add(0, cards.remove(i));
                    }
                    break;
                case "theme":
                    if (c.getTheme().equals(value)) {
                        out.add(0, cards.remove(i));
                    }
                    break;
                case "placement":
                    if (c.getPlacement().equals(value)) {
                        out.add(0, cards.remove(i));
                    }
                    break;
                default:
                    // unknown attribute, return what we got
                    return out;
            }
        }
        return out;
    }
}
