// Card.java
// Quick & dirty, Basic-set only, refactored to reduce duplication

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Card implements Comparable<Card> {

    // ---------- Public fields (keep simple for the take-home) ----------
    public String name, theme, type, cost, oneOf;
    public String victoryPoints, CP, SP, FP, PP, LP, KP, cardText;
    public String germanName, Requires, protectionOrRemoval;
    public String placement;

    private Logic logic;

    // Regions track “stored” resources by rotating; here we model it as an int
    // (0..3)
    public int regionProduction = 0;
    // Regions use production die faces (1..6). 0 means “not a region” / unassigned.
    public int diceRoll = 0;

    // ---------- Global piles for the Basic set ----------
    public static Vector<Card> regions = new Vector<>();
    public static Vector<Card> roads = new Vector<>();
    public static Vector<Card> settlements = new Vector<>();
    public static Vector<Card> cities = new Vector<>();
    public static Vector<Card> events = new Vector<>();
    public static Vector<Card> drawStack1 = new Vector<>();
    public static Vector<Card> drawStack2 = new Vector<>();
    public static Vector<Card> drawStack3 = new Vector<>();
    public static Vector<Card> drawStack4 = new Vector<>();

    // ---------- Construction ----------
    public Card() {
    }

    public Card(String name, String theme, String type,
            String germanName, String placement,
            String oneOf, String cost,
            String victoryPoints, String CP, String SP, String FP,
            String PP, String LP, String KP, String Requires,
            String cardText, String protectionOrRemoval) {
        this.name = name;
        this.theme = theme;
        this.type = type;
        this.germanName = germanName;
        this.placement = placement;
        this.oneOf = oneOf;
        this.cost = cost;
        this.victoryPoints = victoryPoints;
        this.CP = CP;
        this.SP = SP;
        this.FP = FP;
        this.PP = PP;
        this.LP = LP;
        this.KP = KP;
        this.Requires = Requires;
        this.cardText = cardText;
        this.protectionOrRemoval = protectionOrRemoval;

        this.logic = LogicFactory.createLogic(placement, name, type);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Card o) {
        return this.name.compareToIgnoreCase(o.name);
    }

    // ---------- Tiny helpers (kept local so the file stays self-contained)
    // ----------
    static boolean nmEquals(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }

    static boolean nmAt(Card c, String a, String b) {
        if (c == null || c.name == null)
            return false;
        String n = c.name;
        return n.equalsIgnoreCase(a) || n.equalsIgnoreCase(b);
    }

    static int asInt(String s, int def) {
        try {
            if (s == null)
                return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    static String gs(JsonObject o, String k) {
        if (!o.has(k))
            return null;
        JsonElement e = o.get(k);
        return (e == null || e.isJsonNull()) ? null : e.getAsString();
    }

    static int gi(JsonObject o, String k, int def) {
        if (!o.has(k))
            return def;
        try {
            return o.get(k).getAsInt();
        } catch (Exception e) {
            return def;
        }
    }

    // Pop first card by name (case-insensitive) from a vector
    // BUG: when initializing principality we don't seem to remove the item so it
    // overwrites...
    public static Card popCardByName(Vector<Card> cards, String name) {
        if (cards == null || name == null)
            return null;
        String target = name.trim();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            if (c != null && c.name != null && c.name.trim().equalsIgnoreCase(target)) {
                // Remove-by-index guarantees we return a unique instance and it’s gone from the
                // deck
                return cards.remove(i);
            }
        }
        return null; // not found
    }

    // Extract all cards whose public String field `attribute` equals `value`
    public static Vector<Card> extractCardsByAttribute(Vector<Card> cards, String attribute, String value) {
        Vector<Card> out = new Vector<>();
        try {
            java.lang.reflect.Field f = Card.class.getField(attribute);
            for (int i = cards.size() - 1; i >= 0; i--) {
                Card c = cards.get(i);
                Object v = f.get(c);
                if (v != null && String.valueOf(v).equalsIgnoreCase(value)) {
                    out.add(0, cards.remove(i));
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    // ---------- Loading ONLY the Basic set into piles ----------
    public static Vector<Card> loadThemeCards(String jsonPath, String desiredTheme, boolean loadmuliple) throws IOException {
        Vector<Card> allBasic = new Vector<>();

        try (FileReader fr = new FileReader(jsonPath)) {
            JsonElement root = JsonParser.parseReader(fr);
            if (!root.isJsonArray())
                throw new IOException("cards.json: expected top-level array");
            JsonArray arr = root.getAsJsonArray();

            for (JsonElement el : arr) {
                if (!el.isJsonObject())
                    continue;
                JsonObject o = el.getAsJsonObject();
                String theme = gs(o, "theme");
                if (theme == null || !theme.toLowerCase().contains(desiredTheme))
                    continue; 
                int number;
                if (loadmuliple) number = gi(o, "number", 1);
                else number = 1;
                for (int i = 0; i < number; i++) {
                    Card proto = new Card(
                            gs(o, "name"), theme, gs(o, "type"),
                            gs(o, "germanName"), gs(o, "placement"),
                            gs(o, "oneOf"), gs(o, "cost"),
                            gs(o, "victoryPoints"), gs(o, "CP"), gs(o, "SP"), gs(o, "FP"),
                            gs(o, "PP"), gs(o, "LP"), gs(o, "KP"), gs(o, "Requires"),
                            gs(o, "cardText"), gs(o, "protectionOrRemoval"));
                    allBasic.add(proto);
                }
            }
        }
        return allBasic;
    }
    public static void loadBasicCards(String jsonPath) throws IOException {
        //Load Cards and split them into stacks

        Vector<Card> allBasic = loadThemeCards(jsonPath, "basic", true);

        // Split into piles we care about
        // Center cards
        roads = extractCardsByAttribute(allBasic, "name", "Road");
        settlements = extractCardsByAttribute(allBasic, "name", "Settlement");
        cities = extractCardsByAttribute(allBasic, "name", "City");

        // Regions: "type" == "Region"
        regions = extractCardsByAttribute(allBasic, "type", "Region");

        // Events
        events = extractCardsByAttribute(allBasic, "placement", "Event");
        // Place Yule 4th from bottom per cheat sheet
        Card yule = popCardByName(events, "Yule");
        Collections.shuffle(events);
        if (yule != null && events.size() >= 3) {
            events.add(Math.max(0, events.size() - 3), yule);
        }

        // Remaining “draw stack” cards (action/expansion/units)
        Collections.shuffle(allBasic);
        int stackSize = 9; // Intro game
        drawStack1 = new Vector<>(allBasic.subList(0, Math.min(stackSize, allBasic.size())));
        drawStack2 = new Vector<>(allBasic.subList(Math.min(stackSize, allBasic.size()),
                Math.min(2 * stackSize, allBasic.size())));
        drawStack3 = new Vector<>(allBasic.subList(Math.min(2 * stackSize, allBasic.size()),
                Math.min(3 * stackSize, allBasic.size())));
        drawStack4 = new Vector<>(allBasic.subList(Math.min(3 * stackSize, allBasic.size()),
                Math.min(4 * stackSize, allBasic.size())));
    }


    // ---------- Placement validations (ugly but centralized) ----------
    public static boolean isAboveOrBelowSettlementOrCity(Player p, int row, int col) {
        // Inner ring: ±1 from center settlement/city
        Card up1 = p.getCard(row - 1, col);
        Card down1 = p.getCard(row + 1, col);
        if (nmAt(up1, "Settlement", "City"))
            return true;
        if (nmAt(down1, "Settlement", "City"))
            return true;

        // Outer ring allowed *only* if the inner slot is already filled (fill inner
        // first)
        Card up2 = p.getCard(row - 2, col);
        Card down2 = p.getCard(row + 2, col);
        boolean outerOK = ((nmAt(up2, "City", "City") || nmAt(up2, "Settlement", "Settlement")) && up1 != null) ||
                ((nmAt(down2, "City", "City") || nmAt(down2, "Settlement", "Settlement")) && down1 != null);

        return outerOK;
    }

    private static boolean isCenterSlot(int row) {
        return row == 2; 
    }

    // Determine if region name matches what a booster affects
    public static boolean buildingBoostsRegion(String buildingName, String regionName) {
        if (buildingName == null || regionName == null)
            return false;
        else if (buildingName.equalsIgnoreCase("Iron Foundry") && regionName.equalsIgnoreCase("Mountain"))
            return true;
        else if (buildingName.equalsIgnoreCase("Grain Mill") && regionName.equalsIgnoreCase("Field"))
            return true;
        else if (buildingName.equalsIgnoreCase("Lumber Camp") && regionName.equalsIgnoreCase("Forest"))
            return true;
        else if (buildingName.equalsIgnoreCase("Brick Factory") && regionName.equalsIgnoreCase("Hill"))
            return true;
        else if (buildingName.equalsIgnoreCase("Weaver’s Shop") && regionName.equalsIgnoreCase("Pasture"))
            return true;
        else if (buildingName.equalsIgnoreCase("Weaver's Shop") && regionName.equalsIgnoreCase("Pasture"))
            return true; // ascii
        return false;
    }

    // Place the two diagonal regions after a new settlement (ugly prompt kept here)
    public void placeTwoDiagonalRegions(Player active, int row, int col) {
        // Decide which side is the “open side” (the side without a road)
        int colMod = (active.getCard(row, col - 1) == null) ? -1 : 1;
        int sideCol = col + colMod;

        // Draw or choose 2 regions
        Card first, second;

        if (active.flags.contains("SCOUT_NEXT_SETTLEMENT")) {
            // SCOUT: let player pick two specific regions from the region stack by name or
            // index
            active.sendMessage("PROMPT: SCOUT - Choose first region (name or index):");
            String s1 = active.receiveMessage();
            first = pickRegionFromStackByNameOrIndex(s1);
            if (first == null) {
                // fallback to top
                first = Card.regions.isEmpty() ? null : Card.regions.remove(0);
            }

            active.sendMessage("PROMPT: SCOUT - Choose second region (name or index):");
            String s2 = active.receiveMessage();
            second = pickRegionFromStackByNameOrIndex(s2);
            if (second == null) {
                second = Card.regions.isEmpty() ? null : Card.regions.remove(0);
            }

            if (first == null || second == null) {
                active.sendMessage("SCOUT: Region stack exhausted.");
                // still clear the flag to avoid leaking it
                active.flags.remove("SCOUT_NEXT_SETTLEMENT");
                return;
            }
        } else {
            // normal: take top two
            if (Card.regions.size() < 2) {
                active.sendMessage("Region stack does not have two cards.");
                return;
            }
            first = Card.regions.remove(0);
            second = Card.regions.remove(0);
        }

        // Tell the player which two we drew/selected
        active.sendMessage("New settlement regions drawn/selected:");
        active.sendMessage("  1) " + first.name + "   2) " + second.name);

        // Ask where to put the first one (top/bottom), second goes to the other
        active.sendMessage("PROMPT: Place FIRST region on " + (colMod == -1 ? "LEFT" : "RIGHT")
                + " side: TOP or BOTTOM? (T/B)");
        String choice = active.receiveMessage();
        boolean top = choice != null && choice.trim().toUpperCase().startsWith("T");
        row = 2; // center row
        int topRow = row - 1;
        int bottomRow = row + 1;

        if (top) {
            active.placeCard(topRow, sideCol, first);
            active.placeCard(bottomRow, sideCol, second);
        } else {
            active.placeCard(topRow, sideCol, second);
            active.placeCard(bottomRow, sideCol, first);
        }

        // SCOUT benefit is consumed now; clear the flag
        active.flags.remove("SCOUT_NEXT_SETTLEMENT");
    }

    // Helper: choose region by name or index from Card.regions
    private Card pickRegionFromStackByNameOrIndex(String spec) {
        if (spec == null || spec.isBlank())
            return null;
        spec = spec.trim();
        // try index
        try {
            int idx = Integer.parseInt(spec);
            if (idx >= 0 && idx < Card.regions.size()) {
                return Card.regions.remove(idx);
            }
        } catch (Exception ignored) {
        }
        // try by name (first match)
        for (int i = 0; i < Card.regions.size(); i++) {
            Card c = Card.regions.get(i);
            if (c != null && c.name != null && c.name.equalsIgnoreCase(spec)) {
                return Card.regions.remove(i);
            }
        }
        return null;
    }

    // ---------- Main effect / placement entry ----------
    // Returns true if placed/applied; false if illegal placement
    public boolean applyEffect(Player active, Player other, int row, int col) {
        return logic.applyEffect(active, other, row, col, this);
    }

    public boolean is_Valid_placement_extentions(Player active, int row, int col, String nm) {
        if (!isAboveOrBelowSettlementOrCity(active, row, col)) {
            active.sendMessage("Expansion must be above/below a Settlement or City (fill inner ring first).");
            return true;
        }
        // one-of check (simple)
        if (oneOf != null && oneOf.trim().equalsIgnoreCase("1x")) {
            if (active.hasInPrincipality(nm)) {
                active.sendMessage("You may only have one '" + nm + "' in your principality.");
                return true;
            }
        }
        return false;
    }

    public void place_building(int row,int col,Card card, Player player){
                    player.placeCard(row, col, this);
                System.out.println("Contained Building");
                if (nmEquals(card.name, "Abbey")) {
                    player.progressPoints += 1;
                } else if (nmEquals(card.name, "Marketplace")) {
                    player.flags.add("MARKETPLACE");
                } else if (nmEquals(card.name, "Parish Hall")) {
                    player.flags.add("PARISH");
                } else if (nmEquals(card.name, "Storehouse")) {
                    player.flags.add("STOREHOUSE@" + row + "," + col);
                } else if (nmEquals(card.name, "Toll Bridge")) {
                    player.flags.add("TOLLB");
                }

    }

    // ----- tiny helpers used above -----
    public boolean isRegionCard(Card c) {
        return c != null && c.type != null && c.type.equalsIgnoreCase("Region");
    }

    public boolean isExpansionCard(Card c) {
        if (c == null)
            return false;
        String pl = (placement == null ? "" : placement.toLowerCase());
        return pl.contains("settlement/city");
    }
}