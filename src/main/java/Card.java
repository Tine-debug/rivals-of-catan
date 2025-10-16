// Card.java
// Quick & dirty, Basic-set only, refactored to reduce duplication

import java.io.IOException;
import java.util.Vector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
    public static Cardstacks stacks = Cardstacks.getInstance();

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

    public static Card popCardByName(Vector<Card> cards, String name) {
        if (cards == null || name == null)
            return null;
        String target = name.trim();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            if (c != null && c.name != null && c.name.trim().equalsIgnoreCase(target)) {
                return cards.remove(i);
            }
        }
        return null; 
    }

   
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

    public static void loadBasicCards(String jsonPath) throws IOException {
        Cardstacks.loadBasicCards(jsonPath);      
    }


   

    public boolean applyEffect(Player active, Player other, int row, int col) {
        String nm = (this.name == null ? "" : this.name);
        System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");
        return logic.applyEffect(active, other, row, col, this);
    }


}