// Card.java
// Quick & dirty, Basic-set only, refactored to reduce duplication

import java.io.IOException;

public class Card implements Comparable<Card> {

    // ---------- Public fields (keep simple for the take-home) ----------
    public String name, theme, type, cost, oneOf;
    public String cardText;

    public Points points;

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
        this.placement = placement;
        this.oneOf = oneOf;
        this.cost = cost;
        this.points = new Points(victoryPoints, CP, SP, FP, PP, LP, KP);
        this.cardText = cardText;

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

    public static void loadBasicCards(String jsonPath) throws IOException {
        Cardstacks.loadBasicCards(jsonPath);
    }

    public boolean applyEffect(Player active, Player other, int row, int col) {
        String nm = (this.name == null ? "" : this.name);
        System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");
        return logic.applyEffect(active, other, row, col, this);
    }

    public String summarizePoints(){
        return points.summarizePoints();
    }

}
