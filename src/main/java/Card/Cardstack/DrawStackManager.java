package Card.Cardstack;

import Card.Card;
import Player.Player;

import java.util.*;
import java.io.IOException;

public class DrawStackManager {

    private List<Card> drawStack1 = new ArrayList<>();
    private List<Card> drawStack2 = new ArrayList<>();
    private List<Card> drawStack3 = new ArrayList<>();
    private List<Card> drawStack4 = new ArrayList<>();

    public void initializeStacks(List<Card> allCards, boolean shuffle, int size) throws IOException{
        int stackSize = 9;
        if (shuffle) Collections.shuffle(allCards);
        if (allCards.size() < 1) {
            throw new IOException("No cards loaded into drawstacks. All Size: " + size);
        }

        drawStack1 = new ArrayList<>(allCards.subList(0, Math.min(stackSize, allCards.size())));
        drawStack2 = new ArrayList<>(allCards.subList(Math.min(stackSize, allCards.size()),
                Math.min(2 * stackSize, allCards.size())));
        drawStack3 = new ArrayList<>(allCards.subList(Math.min(2 * stackSize, allCards.size()),
                Math.min(3 * stackSize, allCards.size())));
        drawStack4 = new ArrayList<>(allCards.subList(Math.min(3 * stackSize, allCards.size()),
                Math.min(4 * stackSize, allCards.size())));
    }

    private List<Card> stackBy(int n) {
        switch (n) {
            case 1:
                return drawStack1;
            case 2:
                return drawStack2;
            case 3:
                return drawStack3;
            case 4:
                return drawStack4;
            default:
                return drawStack1;
        }
    }

    public void placeAtBottom(Card card, int stackNum) {
        if (card == null) {
            return;
        }
        List<Card> stack = stackBy(stackNum);
        stack.add(card);
    }

    public void drawCard(int which, Player p) {
        List<Card> stack = stackBy(which);
        if (stack.isEmpty()) {
            p.sendMessage("That stack is empty.");
            return;
        }
        p.addToHand(stack.remove(0));
    }

    public void drawCardFromStack(int which, Player p) {
        List<Card> stack = stackBy(which);
        if (stack.isEmpty()) {
            int tries = 0;
            do {
                which = 1 + (which % 4);
                stack = stackBy(which);
                tries++;
            } while (stack.isEmpty() && tries <= 4);

            if (stack.isEmpty()) {
                p.sendMessage("All stacks empty.");
                return;
            }
        }
        p.addToHand(stack.remove(0));
    }

    public void chooseCard(int chosen, Player p) {
        List<Card> stack = stackBy(chosen);
        if (stack.isEmpty()) {
            p.sendMessage("That stack is empty.");
            return;
        }
        p.sendMessage("Stack contains (top..bottom):");
        for (Card c : stack) {
            p.sendMessage(" - " + c.toString());
        }
        p.sendMessage("PROMPT: Type exact name to take:");
        String take = p.receiveMessage();
        for (int i = 0; i < stack.size(); i++) {
            if (stack.get(i).getName().equalsIgnoreCase(take)) {
                p.addToHand(stack.remove(i));
                return;
            }
        }
        p.sendMessage("Not found; no card taken.");
    }
}
