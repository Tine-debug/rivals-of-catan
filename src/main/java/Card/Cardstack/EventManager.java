package Card.Cardstack;

import Card.Card;

import java.util.*;

public class EventManager {

    private List<Card> events = new ArrayList<>();
    private List<Card> resolved = new ArrayList<>();

    public void initializeEvents(List<Card> allCards, boolean shuffle) {
        events = CardUtils.extractBy(allCards, "placement", "Event");
        Card yule = CardUtils.popCardByName(events, "Yule");
        if (shuffle) Collections.shuffle(events);
        if (yule != null && events.size() >= 3) {
            events.add(Math.max(0, events.size() - 3), yule);
        }
    }

    public void reset() {
        events.addAll(resolved);
        resolved.clear();
        Card yule = CardUtils.popCardByName(events, "Yule");
        Collections.shuffle(events);
        if (yule != null && events.size() >= 3) {
            events.add(Math.max(0, events.size() - 3), yule);
        }
    }

    public Card draw() {
        if (events.isEmpty()) return null;
        Card c = events.remove(0);
        resolved.add(c);
        return c;
    }
}
