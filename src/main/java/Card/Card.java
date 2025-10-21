package Card;

import java.io.IOException;
import Card.logic.*;
import Points.Points;
import Player.Player;

public class Card implements Comparable<Card> {

    // ---------- Public fields (keep simple for the take-home) ----------
    private String name, theme, type, cost;
    private boolean oneOf;
    private String cardText;

    private Points points;

    private String placement;

    private Logic logic;

    // Regions track “stored” resources by rotating; here we model it as an int
    // (0..3)
    private int regionProduction = 0;
    // Regions use production die faces (1..6). 0 means “not a region” / unassigned.
    private int diceRoll = 0;

    // ---------- Construction ----------
    public Card() {
    }

    protected Card(String name, String theme, String type,
        String placement, boolean oneOf, String cost,
        Points points, String cardText, Logic logic) {
        this.name = name;
        this.theme = theme;
        this.type = type;
        this.placement = placement;
        this.oneOf = oneOf;
        this.cost = cost;
        this.points = points;
        this.cardText = cardText;
        this.logic = logic;
    }


    //getters
    public String getTheme() {
        return theme;
    }

    public String getType() {
        return type;
    }

    public String getCost() {
        return cost;
    }

    public boolean getOneOf() {
        return oneOf;
    }

    public String getCardText() {
        return cardText;
    }

    public String getName() {
        return name;
    }

    public String getPlacement() {
        return placement;
    }

    public Points getPoints() {
        return points;
    }

    public int getRegionProduction() {
        return regionProduction;
    }

    public int getdiceRoll() {
        return diceRoll;
    }

    public void setPoints(Points newPoints) {
        points = newPoints;

    }

    public void setRegionProduction(int newcount) {
        if (newcount >= 0 && newcount <= 3) {
            regionProduction = newcount;
        }

    }

    public void setDiceRoll(int newdice) {
        if (newdice >= 0 && newdice <= 6) {
            diceRoll = newdice;
        }

    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        }
        return "?";
    }

    @Override
    public int compareTo(Card o) {
        return this.name.compareToIgnoreCase(o.name);
    }

    public boolean applyEffect(Player active, Player other, int row, int col) {
        String nm = (this.name == null ? "" : this.name);
        System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");
        return logic.applyEffect(active, other, row, col, this);
    }

    public String summarizePoints() {
        return points.summarizePoints();
    }

}
