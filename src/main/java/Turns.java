
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class Turns {

    public Turns(List<Player> players) {
        this.players = players;
    }

    private static final int EV_BRIGAND = 1;
    private static final int EV_TRADE = 2;
    private static final int EV_CELEB = 3;
    private static final int EV_PLENTY = 4;
    private static final int EV_EVENT_A = 5;
    private static final int EV_EVENT_B = 6;

    private static final Map<String, String> REGION_TO_RESOURCE = Map.of(
            "Forest", "Lumber",
            "Field", "Grain",
            "Pasture", "Wool",
            "Hill", "Brick",
            "Mountain", "Ore",
            "Gold Field", "Gold");

    private Cardstacks stacks = Cardstacks.getInstance();

    private final Random rng = new Random();
    List<Player> players;

    public int resolveOneTurn(int current) {
        Player active = players.get(current);
        Player other = players.get((current + 1) % players.size());

        int eventFace = rollEventDie(active);
        int prodFace = rollProductionDie(active);

        if (eventFace == EV_BRIGAND) {
            resolveEvent(eventFace, active, other);
            applyProduction(prodFace);
        } else {
            applyProduction(prodFace);
            resolveEvent(eventFace, active, other);
        }

        for (int i = 0; i < players.size(); i++) {
            players.get(i).sendMessage("Opponent's board:");
            players.get(i).sendMessage(
                    "\t\t" + players.get((i + 1) % players.size()).printPrincipality().replace("\n", "\n\t\t"));
            players.get(i).sendMessage("Your board:");
            players.get(i).sendMessage(players.get(i).printPrincipality());
            players.get(i).sendMessage("Your hand:");
            players.get(i).sendMessage(players.get(i).printHand());
        }

        actionPhase(active, other);

        replenish(active);

        exchangePhase(active);
        if (checkWinEndOfTurn(active, other)) {
            return 3;
        }

        current = (current + 1) % players.size();

        return current;

    }

    protected int rollEventDie(Player active) {
        int face = 1 + rng.nextInt(6);
        broadcast("[EventDie] -> " + face);
        return face;
    }

    protected int rollProductionDie(Player active) {
        int face = 1 + rng.nextInt(6);
        if (active.flags.contains("BRIGITTA")) {
            active.sendMessage("PROMPT: Brigitta active -  choose production die [1-6]:");
            try {
                int forced = Integer.parseInt(active.receiveMessage().trim());
                if (forced >= 1 && forced <= 6) {
                    face = forced;
                }
            } catch (Exception ignored) {
            }
            active.flags.remove("BRIGITTA");
        }
        broadcast("[ProductionDie] -> " + face);
        return face;
    }

    private void resolveEvent(int face, Player active, Player other) {
        switch (face) {
            case EV_BRIGAND:
                broadcast("[Event] Brigand Attack");
                for (Player p : players) {
                    int total = countGoldAndWool(p, true);
                    if (total > 7) {
                        zeroGoldAndWool(p, true);
                        p.sendMessage("Brigands! You lose all Gold & Wool in affected regions.");
                    }
                }
                break;

            case EV_TRADE:
                broadcast("[Event] Trade");
                for (Player p : players) {
                    if (p.points.commercePoints >= 3) {
                        p.sendMessage(
                                "PROMPT: Trade Advantage - gain 1 resource of your choice [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                        p.gainResource(p.receiveMessage());
                    }
                }
                break;

            case EV_CELEB:
                broadcast("[Event] Celebration");
                int aSP = players.get(0).points.skillPoints;
                int bSP = players.get(1).points.skillPoints;
                if (aSP == bSP) {
                    for (Player p : players) {
                        p.sendMessage(
                                "PROMPT: Celebration - gain 1 resource of your choice [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                        p.gainResource(p.receiveMessage());
                    }
                } else {
                    Player winner = aSP > bSP ? players.get(0) : players.get(1);
                    winner.sendMessage(
                            "PROMPT: Celebration (you have most skill) - gain 1 resource of your choice [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    winner.gainResource(winner.receiveMessage());
                }
                break;

            case EV_PLENTY:
                broadcast("[Event] Plentiful Harvest: each player gains 1 of choice.");
                for (Player p : players) {
                    p.sendMessage("PROMPT: Plentiful Harvest - choose a resource [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    p.gainResource(p.receiveMessage());
                    if (p.flags.contains("TOLLB")) {
                        int add = grantGoldIfSpace(p, 2);
                        if (add > 0) {
                            p.sendMessage("Toll Bridge: +" + add + " Gold");
                        }
                    }
                }
                break;

            case EV_EVENT_A:
            case EV_EVENT_B: {
                broadcast("[Event] Draw Event Card");

                Card top = stacks.drawEventCard();
                if (top == null) {
                    broadcast("Event deck empty.");
                    break;
                }
                broadcast("EVENT: " + (top.cardText != null ? top.cardText : top.name));

                String nm = (top.name == null ? "" : top.name).toLowerCase();

                Event event = EventFactory.createEvent(nm);
                event.resolve(active, other);

                break;
            }
            default:
                broadcast("[Event] Unknown face " + face);
        }
    }

    private void broadcast(String s) {
        // send to each player
        for (Player p : players) {
            if (p != null) {
                p.sendMessage(s);
            }
        }
    }

    private void applyProduction(int face) {
        for (Player p : players) {
            boolean hasMarketplace = p.flags.contains("MARKETPLACE");
            int pMatches = countFaceRegions(p, face);
            int oppMatches = countFaceRegions(opponentOf(p), face);

            for (int r = 0; r < p.principality.principality.size(); r++) {
                List<Card> row = p.principality.principality.get(r);
                for (int c = 0; c < row.size(); c++) {
                    Card card = row.get(c);
                    if (card == null || !"Region".equalsIgnoreCase(card.type)) {
                        continue;
                    }
                    if (card.diceRoll != face) {
                        continue;
                    }
                    int inc = 1;
                    if (hasAdjacentBoosterForRegion(p, r, c)) {
                        inc += 1;
                    }

                    card.regionProduction = Math.min(3, card.regionProduction + inc);
                }
            }

            if (hasMarketplace && oppMatches > pMatches) {
                p.sendMessage("PROMPT: Marketplace - choose one resource produced on face " + face
                        + " to gain (e.g., Grain/Gold/Lumber):");
                String res = p.receiveMessage();
                p.gainResource(res);
            }
        }
    }

    private void actionPhase(Player active, Player other) {
        boolean done = false;
        active.sendMessage("Opponent's board:");
        active.sendMessage("\t\t" + other.printPrincipality().replace("\n", "\n\t\t"));
        while (!done) {
            active.sendMessage("Your board:");
            active.sendMessage(active.printPrincipality());
            active.sendMessage("Your hand:");
            active.sendMessage(active.printHand());
            active.sendMessage("Action Phase:");
            active.sendMessage("  TRADE3 <get> <give>     — bank 3:1 ([Brick|Grain|Lumber|Wool|Ore|Gold])");
            active.sendMessage(
                    "  TRADE2 <get> <Res>      — if you have a 2:1 ship for <Res> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
            active.sendMessage(
                    "  LTS <L|R> <2from> <1to> — Large Trade Ship adjacent trade (left/right side) ([Brick|Grain|Lumber|Wool|Ore|Gold])");
            String play = "  PLAY <cardName> | <id>  — play a card from hand / play center card: ";

            ArrayList<String> buildBits = stacks.getCenterbuildingCost();

            play += String.join(", ", buildBits);
            active.sendMessage(play);
            active.sendMessage("  END                     — finish action phase");
            active.sendMessage("PROMPT: make your choice: ");
            String cmd = active.receiveMessage();
            if (cmd == null) {
                cmd = "END";
            }
            String up = cmd.trim().toUpperCase(Locale.ROOT);

            if (up.startsWith("TRADE3")) {
                String[] parts = cmd.trim().split("\\s+");
                if (parts.length >= 3) {
                    String get = parts[1];
                    String give = parts[2];
                    if (active.getResourceCount(give) >= 3) {
                        active.removeResource(give, 3);
                        active.gainResource(get);
                        broadcast("Trade 3:1 -> +1 " + get);
                    } else {
                        active.sendMessage("Not enough " + give + " to trade 3:1.");
                    }
                } else {
                    active.sendMessage("Usage: TRADE3 <get> <give> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                }
            } else if (up.startsWith("TRADE2")) {
                String[] parts = cmd.trim().split("\\s+");
                if (parts.length >= 3) {
                    String get = parts[1];
                    String from = parts[2].toUpperCase();
                    if (active.flags.contains("2FOR1_" + from)) {
                        if (active.getResourceCount(from) >= 2) {
                            active.removeResource(from, 2);
                            active.gainResource(get);
                            broadcast("Trade 2:1 (" + from + " ship) -> +1 " + get);
                        } else {
                            active.sendMessage("Not enough " + from + " to trade 2:1.");
                        }
                    } else {
                        active.sendMessage("You don't have a 2:1 ship for " + from + ".");
                    }
                } else {
                    active.sendMessage("Usage: TRADE2 <get> <give> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                }
            } else if (up.startsWith("LTS")) {
                String[] parts = cmd.trim().split("\\s+");
                if (parts.length >= 4) {
                    String side = parts[1].toUpperCase(); // L or R
                    String twoFrom = parts[2];
                    String oneTo = parts[3];
                    if (applyLTS(active, side, twoFrom, oneTo)) {
                        broadcast("LTS: traded 2 " + twoFrom + " for 1 " + oneTo + " on the "
                                + (side.startsWith("L") ? "LEFT" : "RIGHT"));
                    } else {
                        active.sendMessage("LTS trade invalid here.");
                    }
                } else {
                    active.sendMessage("Usage: LTS <L|R> <2from> <1to> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                }
            } else if (up.startsWith("PLAY")) {
                String[] parts = cmd.trim().split("\\s+", 2);
                if (parts.length < 2) {
                    active.sendMessage("Usage: PLAY <cardName> | <id>");
                    continue;
                }
                String spec = parts[1].trim();

                // ---------- 1) Center cards from piles: Road / Settlement / City ----------
                if (spec.equalsIgnoreCase("Road") || spec.equalsIgnoreCase("Settlement")
                        || spec.equalsIgnoreCase("City")) {
                    int[] coordinatesplaced = stacks.placeCenterCard(active, other, spec);
                    if (coordinatesplaced[0] != -1 && coordinatesplaced[1] != -1) {
                        broadcast("Built " + spec + " at (" + coordinatesplaced[0] + "," + coordinatesplaced[1] + ")");
                        continue;
                    }
                    continue;
                }

                Card c = active.hand.findCardInHand(active, spec);
                if (c == null) {
                    active.sendMessage("No such card in hand: " + spec);
                    continue;
                }
                if (!active.payCost(c.cost)) {
                    active.sendMessage("Can't afford cost: " + (c.cost == null ? "-" : c.cost));
                    continue;
                }

                boolean isAction = (c.type != null && c.type.toLowerCase().contains("action"));
                boolean ok;

                if (isAction) {

                    ok = c.applyEffect(active, other, -1, -1);
                    if (!ok) {
                        active.sendMessage("Action could not be resolved; refunding cost.");
                        active.refundCost(c.cost);
                        continue;
                    }

                    active.hand.hand.remove(c);
                    broadcast("Current player played action " + c.name);
                } else {
                    // Non-action: needs placement
                    active.sendMessage("PROMPT: Enter placement coordinates as: ROW COL");
                    int row = -1, col = -1;
                    try {
                        String[] rc = active.receiveMessage().trim().split("\\s+");
                        row = Integer.parseInt(rc[0]);
                        col = Integer.parseInt(rc[1]);
                    } catch (Exception e) {
                        active.sendMessage("Invalid coordinates. Use: ROW COL (e.g., 2 3)");
                        active.refundCost(c.cost);
                        continue;
                    }

                    ok = c.applyEffect(active, other, row, col);
                    if (!ok) {
                        active.sendMessage("Illegal placement/effect; refunding cost.");
                        active.refundCost(c.cost);
                        continue;
                    }

                    active.hand.hand.remove(c);
                    broadcast("Current player played " + c.name + " at (" + row + "," + col + ")");
                }
            } else if (up.startsWith("END")) {
                done = true;
            } else {
                active.sendMessage("Unknown command.");
            }
        }
    }

    public void replenish(Player p) {
        if (p.flags != null && p.flags.remove("NO_REPLENISH_ONCE")) {
            p.sendMessage("You cannot replenish your hand this turn (Fraternal Feuds).");
        } else {
            int handTarget = 3 + p.points.progressPoints;
            while (p.handSize() < handTarget) {
                p.sendMessage("PROMPT: Replenish - choose draw stack [1-4]:");
                int which = readInt(p.receiveMessage(), 1);
                stacks.drawCardfromStack(which, p);
            }
        }
    }

    private void exchangePhase(Player p) {
        int limit = 3 + p.points.progressPoints;
        if (p.handSize() < limit) {
            broadcast("Exchange: hand below limit; skipping.");
            return;
        }

        p.sendMessage("PROMPT: Exchange a card? (Y/N)");
        String ans = p.receiveMessage();
        if (ans == null || !ans.trim().toUpperCase().startsWith("Y")) {
            return;
        }

        p.sendMessage("PROMPT: Enter card name to put under a stack:");
        String nm = p.receiveMessage();
        Card chosen = p.removeFromHandByName(nm);
        if (chosen == null) {
            p.sendMessage("Not in hand.");
            return;
        }

        p.sendMessage("PROMPT: Choose stack [1-4] to put it under:");
        int st = readInt(p.receiveMessage(), 1);
        stacks.placeCardBottomStack(chosen, st);

        boolean hasParish = p.flags.contains("PARISH");
        int searchCost = hasParish ? 1 : 2;

        p.sendMessage("PROMPT: Choose Random draw (R) or Search (S, costs " + searchCost + " any)?");
        String mode = p.receiveMessage();
        if (mode != null && mode.trim().toUpperCase().startsWith("S")) {
            // Pay 1 (with Parish) or 2 (normal) resources of the player's choice
            if (p.totalAllResources() < searchCost) {
                p.sendMessage("Not enough resources to search.");
                return;
            }
            for (int i = 0; i < searchCost; i++) {
                p.sendMessage("PROMPT: Discard resource #" + (i + 1) + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                p.removeResource(p.receiveMessage(), 1);
            }
            stacks.chooseCardFromStack(st, p);

        } else {
            // Random draw (top of chosen stack)
            stacks.drawCard(st, p);
        }
    }

    private boolean checkWinEndOfTurn(Player active, Player other) {
        int score = active.currentScoreAgainst(other);
        if (score >= 7) {
            broadcast("winner: Player " + players.indexOf(active)
                    + " wins with " + score + " VP (incl. advantage tokens)!");
            return true;
        }
        return false;
    }

    private int countGoldAndWool(Player p, boolean excludeStorehouseAdj) {
        int total = 0;
        Set<String> excluded = excludeStorehouseAdj ? storehouseExcludedKeys(p) : Set.of();
        for (int r = 0; r < p.principality.principality.size(); r++) {
            java.util.List<Card> row = p.principality.principality.get(r);
            if (row == null) {
                continue;
            }
            for (int c = 0; c < row.size(); c++) {
                Card card = row.get(c);
                if (card == null) {
                    continue;
                }
                String key = r + ":" + c;
                if (excluded.contains(key)) {
                    continue;
                }
                if ("Gold Field".equalsIgnoreCase(card.name) || "Pasture".equalsIgnoreCase(card.name)) {
                    total += Math.max(0, Math.min(3, card.regionProduction));
                }
            }
        }

        return total;
    }

    private void zeroGoldAndWool(Player p, boolean excludeStorehouseAdj) {
        Set<String> excluded = excludeStorehouseAdj ? storehouseExcludedKeys(p) : Set.of();
        forEachRegion(p, (r, c, card) -> {
            if (card == null) {
                return;
            }
            String key = r + ":" + c;
            if (excluded.contains(key)) {
                return;
            }
            if ("Gold Field".equalsIgnoreCase(card.name) || "Pasture".equalsIgnoreCase(card.name)) {
                card.regionProduction = 0;
            }
        });
    }

    private int grantGoldIfSpace(Player p, int want) {
        int given = 0;
        for (int r = 0; r < p.principality.principality.size(); r++) {
            java.util.List<Card> row = p.principality.principality.get(r);
            if (row == null) {
                continue;
            }
            for (int c = 0; c < row.size(); c++) {
                if (given >= want) {
                    break; // stop if already satisfied

                }
                Card card = row.get(c);
                if (card != null && "Gold Field".equalsIgnoreCase(card.name)) {
                    int can = Math.max(0, 3 - card.regionProduction);
                    int add = Math.min(can, want - given);
                    if (add > 0) {
                        card.regionProduction += add;
                        given += add;
                    }
                }
            }
        }
        return given;
    }

    private int countFaceRegions(Player p, int face) {
        int n = 0;
        for (List<Card> row : p.principality.principality) {
            for (Card c : row) {
                if (c != null && "Region".equalsIgnoreCase(c.type) && c.diceRoll == face) {
                    n++;
                }
            }
        }
        return n;
    }

    private Player opponentOf(Player p) {
        return (p == players.get(0)) ? players.get(1) : players.get(0);
    }

    private boolean hasAdjacentBoosterForRegion(Player p, int rr, int cc) {
        Card region = p.getCard(rr, cc);
        if (region == null) {
            return false;
        }
        Card left = p.getCard(rr, cc - 1);
        Card right = p.getCard(rr, cc + 1);
        return isBoosting(left, region) || isBoosting(right, region);
    }

    private boolean applyLTS(Player p, String side, String twoFrom, String oneTo) {
        // Find any LTS flag; for simplicity use the first one
        int ltsRow = -1, ltsCol = -1;
        for (String f : p.flags) {
            if (f.startsWith("LTS@")) {
                String[] rc = f.substring(4).split(",");
                try {
                    ltsRow = Integer.parseInt(rc[0]);
                    ltsCol = Integer.parseInt(rc[1]);
                } catch (Exception ignored) {
                }
                break;
            }
        }
        if (ltsRow < 0) {
            return false;
        }

        // Regions on that side are at (ltsRow, ltsCol-1) and (ltsRow, ltsCol+1)
        int takeCol = side.startsWith("L") ? ltsCol - 1 : ltsCol + 1;
        Card fromRegion = p.getCard(ltsRow, takeCol);
        Card toRegion = p.getCard(ltsRow, (side.startsWith("L") ? ltsCol + 1 : ltsCol - 1));

        if (fromRegion == null || toRegion == null) {
            return false;
        }

        // We don’t track per-resource piles, but we *do* track regionProduction; allow
        // trade if fromRegion’s
        // produced resource type matches `twoFrom` and has at least 2; grant +1 to
        // `oneTo` by increasing toRegion
        String fromType = REGION_TO_RESOURCE.getOrDefault(fromRegion.name, "");
        if (!fromType.equalsIgnoreCase(twoFrom)) {
            return false;
        }
        if (fromRegion.regionProduction < 2) {
            return false;
        }

        fromRegion.regionProduction -= 2;
        // Grant the “oneTo”: if it matches toRegion’s type, store there; else bank
        String toType = REGION_TO_RESOURCE.getOrDefault(toRegion.name, "");
        if (toType.equalsIgnoreCase(oneTo)) {
            toRegion.regionProduction = Math.min(3, toRegion.regionProduction + 1);
        } else {
            p.gainResource(oneTo);
        }
        return true;
    }

    private int readInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private Set<String> storehouseExcludedKeys(Player p) {
        Set<String> out = new HashSet<>();
        for (int r = 0; r < p.principality.principality.size(); r++) {
            List<Card> row = p.principality.principality.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x != null && x.name != null && x.name.equalsIgnoreCase("Storehouse")) {
                    // Decide side: if there’s a settlement/city below we’re on upper side, else
                    // lower
                    boolean belowCenter = nmAt(p.getCard(r + 1, c), "Settlement", "City")
                            || nmAt(p.getCard(r + 2, c), "City", "City");
                    int regionRow = belowCenter ? r - 1 : r + 1;
                    out.add(regionRow + ":" + (c - 1));
                    out.add(regionRow + ":" + (c + 1));
                }
            }
        }
        return out;
    }

    private interface RegionVisitor {

        void visit(int r, int c, Card card);
    }

    private void forEachRegion(Player p, RegionVisitor v) {
        for (int r = 0; r < p.principality.principality.size(); r++) {
            List<Card> row = p.principality.principality.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card card = row.get(c);
                if (card != null && "Region".equalsIgnoreCase(card.type)) {
                    v.visit(r, c, card);
                }
            }
        }
    }

    private boolean isBoosting(Card maybeBuilding, Card region) {
        if (maybeBuilding == null) {
            return false;
        }
        if (!"Building".equalsIgnoreCase(maybeBuilding.type)) {
            return false;
        }
        return buildingBoostsRegion(maybeBuilding.name, region.name);
    }

    private static boolean nmAt(Card c, String a, String b) {
        if (c == null || c.name == null) {
            return false;
        }
        return c.name.equalsIgnoreCase(a) || c.name.equalsIgnoreCase(b);
    }

        public static boolean buildingBoostsRegion(String buildingName, String regionName) {
        if (buildingName == null || regionName == null) {
            return false;
        } else if (buildingName.equalsIgnoreCase("Iron Foundry") && regionName.equalsIgnoreCase("Mountain")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Grain Mill") && regionName.equalsIgnoreCase("Field")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Lumber Camp") && regionName.equalsIgnoreCase("Forest")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Brick Factory") && regionName.equalsIgnoreCase("Hill")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Weaver’s Shop") && regionName.equalsIgnoreCase("Pasture")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Weaver's Shop") && regionName.equalsIgnoreCase("Pasture")) {
            return true;
        }
        return false;
    }

}
