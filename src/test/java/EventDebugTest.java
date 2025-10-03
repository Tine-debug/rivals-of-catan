import java.lang.reflect.Method;
import java.util.ArrayDeque;

import org.junit.jupiter.api.Test;

public class EventDebugTest {

    static class MockPlayer extends Player {
        final ArrayDeque<String> inputs = new ArrayDeque<>();
        final StringBuilder log = new StringBuilder();

        MockPlayer(String... answers) {
            super();
            for (String a : answers) inputs.add(a);
        }

        @Override public void sendMessage(Object m) { log.append(String.valueOf(m)).append("\n"); }
        @Override public String receiveMessage() {
            return inputs.isEmpty() ? "Wool" : inputs.poll();
        }
    }

    @Test
    public void debugEvents() throws Exception {
        // 1) load cards + init server (start(true) creates two players and initializes principality)
        Card.loadBasicCards("cards.json");
        Server s = new Server();
        s.players.clear();
        MockPlayer p1 = new MockPlayer("Wool","Grain","Ore","Wool","Wool");
        MockPlayer p2 = new MockPlayer("Wool","Grain","Ore","Wool");
        s.players.add(p1);
        s.players.add(p2);

        Method init = Server.class.getDeclaredMethod("initPrincipality");
        init.setAccessible(true);
        init.invoke(s);



        // 2) replace players with MockPlayers but keep initialized principality/hand
        Player protoA = s.players.get(0);
        Player protoB = s.players.get(1);

        p1.principality = protoA.principality; p1.hand = protoA.hand;
        p2.principality = protoB.principality; p2.hand = protoB.hand;
        s.players.set(0, p1); s.players.set(1, p2);

        // 3) Helpers inline (private-ish)
        // ensure existing pastures/gold fields have full stored production
        setAllPastureAndGoldTo(p1, 3);

        // Add extra Pasture and Gold Field to p1 so countGoldAndWool can exceed threshold (>7)
        addRegionByNameToPlayer(p1, "Pasture", 3);
        addRegionByNameToPlayer(p1, "Gold Field", 3);

        // ---- Brigand (face=1) ----
        System.out.println("=== BRIGAND BEFORE ===");
        printResourceSummary(p1);
        printRegionProductions(p1);

        invokeResolveEvent(s, 1, p1, p2);

        System.out.println("=== BRIGAND AFTER ===");
        printResourceSummary(p1);
        printRegionProductions(p1);

        // ---- Trade (face=2) ----
        p1.commercePoints = 3; // give trade advantage
        p2.commercePoints = 0;
        p1.inputs.add("Wool"); // answer the prompt
        System.out.println("=== TRADE BEFORE ===");
        printResourceSummary(p1);
        printResourceSummary(p2);
        invokeResolveEvent(s, 2, p1, p2);
        System.out.println("=== TRADE AFTER ===");
        printResourceSummary(p1);
        printResourceSummary(p2);
        

        // ---- Celebration (face=3) ----
        p1.skillPoints = 4; p2.skillPoints = 0; // p1 wins tie
        p1.inputs.add("Grain"); // answer
        System.out.println("=== CELEBRATION BEFORE ===");
        printResourceSummary(p1);
        invokeResolveEvent(s, 3, p1, p2);
        System.out.println("=== CELEBRATION AFTER ===");
        printResourceSummary(p1);

        // ---- Plentiful Harvest (face=4) ----
        p1.inputs.add("Ore"); p2.inputs.add("Wool");
        System.out.println("=== PLENTIFUL BEFORE ===");
        printResourceSummary(p1); printResourceSummary(p2);
        invokeResolveEvent(s, 4, p1, p2);
        System.out.println("=== PLENTIFUL AFTER ===");
        printResourceSummary(p1); printResourceSummary(p2);

        // Print messages collected from mock player to inspect prompts/log
        System.out.println("----- p1.log -----\n" + p1.log);
        System.out.println("----- p2.log -----\n" + p2.log);
    }

    // ----------------- small helpers -----------------

    private static void invokeResolveEvent(Server s, int face, Player a, Player b) throws Exception {
        Method m = Server.class.getDeclaredMethod("resolveEvent", int.class, Player.class, Player.class);
        m.setAccessible(true);
        m.invoke(s, face, a, b);
    }

    private static void printResourceSummary(Player p) {
        System.out.println("Player resources: Wool=" + p.getResourceCount("Wool")
                + " Gold=" + p.getResourceCount("Gold")
                + " Grain=" + p.getResourceCount("Grain")
                + " Ore=" + p.getResourceCount("Ore"));
    }

    private static void printRegionProductions(Player p) {
        System.out.println("Region productions:");
        for (int r = 0; r < p.principality.size(); r++) {
            for (int c = 0; c < p.principality.get(r).size(); c++) {
                Card card = p.getCard(r, c);
                if (card != null && "Region".equalsIgnoreCase(card.type)) {
                    System.out.println("  [" + r + "," + c + "] " + card.name + " -> stored=" + card.regionProduction);
                }
            }
        }
    }

    private static void setAllPastureAndGoldTo(Player p, int val) {
        for (int r = 0; r < p.principality.size(); r++) {
            for (int c = 0; c < p.principality.get(r).size(); c++) {
                Card card = p.getCard(r, c);
                if (card != null && "Region".equalsIgnoreCase(card.type)
                        && (card.name.equalsIgnoreCase("Pasture") || card.name.equalsIgnoreCase("Gold Field"))) {
                    card.regionProduction = Math.min(3, val);
                }
            }
        }
    }

    private static void addRegionByNameToPlayer(Player p, String name, int production) {
        Card extra = Card.popCardByName(Card.regions, name);
        if (extra == null) {
            System.out.println("No extra region '" + name + "' available in Card.regions");
            return;
        }
        extra.regionProduction = Math.min(3, production);
        // place in first null slot
        for (int r = 0; r < p.principality.size(); r++) {
            for (int c = 0; c < p.principality.get(r).size(); c++) {
                if (p.getCard(r, c) == null) {
                    p.placeCard(r, c, extra);
                    return;
                }
            }
        }
        // if no null slot found, append a new row/col via placeCard (ensureSize handles it)
        p.placeCard(p.principality.size(), 0, extra);
    }
}
