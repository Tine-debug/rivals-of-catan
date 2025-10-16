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
   



    // ---------- Main effect / placement entry ----------
    // Returns true if placed/applied; false if illegal placement
    public boolean applyEffect(Player active, Player other, int row, int col) {
        String nm = (this.name == null ? "" : this.name);
        System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");

        return logic.applyEffect(active, other, row, col, this);
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

}