package Card;

import java.io.IOException;
import Card.logic.*;
import Points.Points;
import Player.Player;

public class Card implements Comparable<Card> {

    // ---------- Public fields (keep simple for the take-home) ----------
    private String name, theme, type, cost;
    private boolean oneOf;
    private  String cardText;

    private Points points;

    private String placement;

    private Logic logic;

    // Regions track “stored” resources by rotating; here we model it as an int
    // (0..3)
    private int regionProduction = 0;
    // Regions use production die faces (1..6). 0 means “not a region” / unassigned.
    private int diceRoll = 0;





    // ---------- Global piles for the Basic set ----------
    private static Cardstacks stacks = Cardstacks.getInstance();

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

        this.oneOf = (oneOf == null) ? false : (oneOf.equalsIgnoreCase("1x"));
        this.cost = cost;
        this.points = new Points(victoryPoints, CP, SP, FP, PP, LP, KP);
        this.cardText = cardText;

        this.logic = LogicFactory.createLogic(placement, name, type);
    }

    public Card(String name, String type, String cost){
        this.name = name;
        this.type = type;
        this.cost = cost;
    }

    public String getTheme(){
        return theme;
    }

    public String getType(){
        return type;
    }

    public String getCost(){
        return cost;
    }

    public boolean getOneOf(){
        return oneOf;
    }

    public String getCardText(){
        return cardText;
    }

    public String getName(){
        return name;
    }

    public String getPlacement(){
        return placement;
    }

    public Points getPoints(){
        return points;
    }

    public void updatePoints(Points newPoints){
        points = newPoints;
        
    }

    public int getRegionProduction(){
        return regionProduction;
    }

    public int getdiceRoll(){
        return diceRoll;
    }

    public void setRegionProduction(int newcount){
        if (newcount >= 0 && newcount <= 3) regionProduction = newcount; 

    }

    public void setDiceRoll(int newdice){
         if (newdice >= 0 && newdice <= 6) diceRoll = newdice; 

    }

   

    @Override
    public String toString() {
        if (name != null) return name;
        return "?";
    }

    @Override
    public int compareTo(Card o) {
        return this.name.compareToIgnoreCase(o.name);
    }

    public static void loadBasicCards(String jsonPath) throws IOException {
        stacks.loadBasicCards(jsonPath);
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
