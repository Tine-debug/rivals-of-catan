
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Cardstacks {

    private static Vector<Card> regions = new Vector<>();
    public static Vector<Card> roads = new Vector<>();
    public static Vector<Card> settlements = new Vector<>();
    public static Vector<Card> cities = new Vector<>();
    public static Vector<Card> events = new Vector<>();
    public static Vector<Card> drawStack1 = new Vector<>();
    public static Vector<Card> drawStack2 = new Vector<>();
    public static Vector<Card> drawStack3 = new Vector<>();
    public static Vector<Card> drawStack4 = new Vector<>();

    private static Cardstacks instance;

    private Cardstacks() {

    }

    public static Cardstacks getInstance() {
        if (instance == null) {
            instance = new Cardstacks();
        }
        return instance;
    }

    public void loadBasicCardsoptionalshuffle(boolean shuffle, String jsonPath) throws IOException {
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
        if (shuffle) {
            Collections.shuffle(events);
        }
        if (yule != null && events.size() >= 3) {
            events.add(Math.max(0, events.size() - 3), yule);
        }

        // Remaining “draw stack” cards (action/expansion/units)
        if (shuffle) {
            Collections.shuffle(allBasic);
        }
        int stackSize = 9;
        drawStack1 = new Vector<>(allBasic.subList(0, Math.min(stackSize, allBasic.size())));
        drawStack2 = new Vector<>(allBasic.subList(Math.min(stackSize, allBasic.size()),
                Math.min(2 * stackSize, allBasic.size())));
        drawStack3 = new Vector<>(allBasic.subList(Math.min(2 * stackSize, allBasic.size()),
                Math.min(3 * stackSize, allBasic.size())));
        drawStack4 = new Vector<>(allBasic.subList(Math.min(3 * stackSize, allBasic.size()),
                Math.min(4 * stackSize, allBasic.size())));
    }

    public void loadBasicCards(String jsonPath) throws IOException {
        loadBasicCardsoptionalshuffle(false, jsonPath);
    }

    public Vector<Card> loadThemeCards(String jsonPath, String desiredTheme, boolean loadmuliple) throws IOException {
        Vector<Card> allBasic = new Vector<>();

        try (FileReader fr = new FileReader(jsonPath)) {
            JsonElement root = JsonParser.parseReader(fr);
            if (!root.isJsonArray()) {
                throw new IOException("cards.json: expected top-level array");
            }
            JsonArray arr = root.getAsJsonArray();

            for (JsonElement el : arr) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject o = el.getAsJsonObject();
                String theme = gs(o, "theme");
                if (theme == null || !theme.toLowerCase().contains(desiredTheme)) {
                    continue;
                }
                int number;
                if (loadmuliple) {
                    number = gi(o, "number", 1); 
                }else {
                    number = 1;
                }
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

    public Card popCardByName(Vector<Card> cards, String name) {
        if (cards == null || name == null) {
            return null;
        }
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
    public Vector<Card> extractCardsByAttribute(Vector<Card> cards, String attribute, String value) {
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

    static String gs(JsonObject o, String k) {
        if (!o.has(k)) {
            return null;
        }
        JsonElement e = o.get(k);
        return (e == null || e.isJsonNull()) ? null : e.getAsString();
    }

    static int gi(JsonObject o, String k, int def) {
        if (!o.has(k)) {
            return def;
        }
        try {
            return o.get(k).getAsInt();
        } catch (Exception e) {
            return def;
        }
    }

    public void inizilizeRegion(Player p, int[][] regionDice, int center, int i) {
        Card forest = popCardByName(Cardstacks.regions, "Forest");
        forest.diceRoll = regionDice[i][0];
        forest.regionProduction = 1;
        Card gold = popCardByName(Cardstacks.regions, "Gold Field");
        gold.diceRoll = regionDice[i][1];
        gold.regionProduction = 0;
        Card field = popCardByName(Cardstacks.regions, "Field");
        field.diceRoll = regionDice[i][2];
        field.regionProduction = 1;
        Card hill = popCardByName(Cardstacks.regions, "Hill");
        hill.diceRoll = regionDice[i][3];
        hill.regionProduction = 1;
        Card past = popCardByName(Cardstacks.regions, "Pasture");
        past.diceRoll = regionDice[i][4];
        past.regionProduction = 1;
        Card mount = popCardByName(Cardstacks.regions, "Mountain");
        mount.diceRoll = regionDice[i][5];
        mount.regionProduction = 1;

        p.placeCard(center - 1, 0, forest);
        p.placeCard(center - 1, 2, gold);
        p.placeCard(center - 1, 4, field);
        p.placeCard(center + 1, 0, hill);
        p.placeCard(center + 1, 2, past);
        p.placeCard(center + 1, 4, mount);
    }

    public void shuffleRegions() {
        Collections.shuffle(Cardstacks.regions);
    }

    public Card findUndicedRegionByName(String name) {
        for (int i = 0; i < regions.size(); i++) {
            Card c = regions.get(i);
            if (c != null && name.equalsIgnoreCase(c.name) && c.diceRoll == 0) {
                return c;
            }
        }
        return null;
    }

        public Card pickRegionFromStackByNameOrIndex(String spec) {
        if (spec == null || spec.isBlank())
            return null;
        spec = spec.trim();
        // try index
        try {
            int idx = Integer.parseInt(spec);
            if (idx >= 0 && idx < Cardstacks.regions.size()) {
                return Cardstacks.regions.remove(idx);
            }
        } catch (Exception ignored) {
        }
        // try by name (first match)
        for (int i = 0; i < Cardstacks.regions.size(); i++) {
            Card c = Cardstacks.regions.get(i);
            if (c != null && c.name != null && c.name.equalsIgnoreCase(spec)) {
                return Cardstacks.regions.remove(i);
            }
        }
        return null;
    }

    public Card drawregionCard(){
        return regions.isEmpty() ? null : regions.remove(0);
    }

    public int getRegionstackSize(){
        return regions.size();
    }

}
