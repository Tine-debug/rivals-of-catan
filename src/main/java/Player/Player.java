package Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import Points.*;
import Card.Card;

public class Player {



    private Points points;
    
    

    //private int tradeRate = 3;
    private boolean isBot = false;

    private Set<String> flags = new HashSet<>();

    private Map<String, Integer> resources = new HashMap<>();

    private final Hand hand = new Hand();

    private final Principality principality;

    private int lastSettlementRow = -1, lastSettlementCol = -1;

    private final Scanner in = new Scanner(System.in);

    public Player() {
        String[] all = {"Brick", "Grain", "Lumber", "Wool", "Ore", "Gold", "Any"};
        principality = new Principality();
        resources = new java.util.HashMap<>();
        for (String r : all) {
            resources.put(r, 0);
        }
        PointsBuilder pointsBuilder = new PointsBuilder();
        pointsBuilder.victoryPoints(2);
        this.points = pointsBuilder.build();
    }

    public void sendMessage(Object m) {
        System.out.println(m);
    }

    public String receiveMessage() {
        System.out.print("> ");
        return in.nextLine();
    }

    //getters
    public Set<String> getFlags(){
        return this.flags;
    }

    public Principality getPrincipality(){
        return principality;
    }

    public Hand getHand(){
        return hand;
    }

    public boolean getIsBot(){
        return isBot;
    }

    public int getLastSettlementRow(){
        return lastSettlementRow;
    }

    public int getLastSettlementCol(){
        return lastSettlementCol;
    }

    //setters
    public void setFlags(Set<String> newFlags){
        this.flags = newFlags;
    }

    public void setIsBot(boolean newvalue){
        this.isBot = newvalue;
    }

    public void setLastSettlementRow(int newvalue){
        this.lastSettlementRow = newvalue;
    }
    
    public void setLastSettlementCol(int newvalue){
        this.lastSettlementCol = newvalue;
    }

    

    //


    public void removeFlag(String toRemove){
        flags.remove(toRemove);
    }

    public Points getPoints(){
        return this.points;
    }

    public void setPoints(Points newValue){
        this.points = newValue;
    }



    public Card getCard(int r, int c) {
        return principality.getCard(r, c);
    }

    public void placeCard(int r, int c, Card card) {
        principality.placeCard(r, c, card);
    }

    public int expandAfterEdgeBuild(int col) {
        return principality.expandAfterEdgeBuild(col);
    }

    public boolean hasInPrincipality(String name) {
        return principality.hasInPrincipality(name);
    }

    public String printPrincipality() {
        return principality.printPrincipality(this);
    }

    public String printHand() {
        return hand.printHand();
    }

    public boolean hasTradeTokenAgainst(Player opp) {
        if (this.points.commercePoints < 3) {
            return false;
        }
        if (opp == null) {
            return true;
        }
        return (this.points.commercePoints > opp.points.commercePoints);
    }

    public boolean hasStrengthTokenAgainst(Player opp) {
        if (this.points.strengthPoints < 3) {
            return false;
        }
        if (opp == null) {
            return true;
        }
        return (this.points.strengthPoints > opp.points.strengthPoints);
    }

    // Final score used for win check: base VP + 1 per advantage token against
    // opponent
    public int currentScoreAgainst(Player opp) {
        int score = this.points.victoryPoints;
        if (hasTradeTokenAgainst(opp)) {
            score += 1;
        }
        if (hasStrengthTokenAgainst(opp)) {
            score += 1;
        }
        return score;
    }

    // ------------- Resources (per-region, not pooled) -------------
    // Map a resource name to its Region card name
    private String resourceToRegion(String type) {
        if (type == null) {
            return null;
        }
        String t = type.trim().toLowerCase();
        switch (t) {
            case "brick":
                return "Hill";
            case "grain":
                return "Field";
            case "lumber":
                return "Forest";
            case "wool":
                return "Pasture";
            case "ore":
                return "Mountain";
            case "gold":
                return "Gold Field";
            case "any":
                return "Any";
            default:
                return null;
        }
    }

    public int totalAllResources() {
        return principality.totalAllResources();
    }

    // Count stored resources of a specific resource type across the board
    public int getResourceCount(String type) {
        return principality.getResourceCount(type);
    }

    // Gain 1 resource of a type: add to the matching region with the LOWEST stock
    // (<3)
    // If "Any", ask the player which resource to take.
    public void gainResource(String type) {
        String t = type;
        if (t == null || t.equalsIgnoreCase("Any")) {
            sendMessage("PROMPT: Choose resource to gain (Brick/Grain/Lumber/Wool/Ore/Gold):");
            t = receiveMessage();
        }
        String regionName = resourceToRegion(t);
        if (regionName == null || "Any".equals(regionName)) {
            sendMessage("Unknown resource '" + t + "'. Ignored.");
            return;
        }

        java.util.List<Card> regs = principality.findRegions(regionName);
        if (regs.isEmpty()) {
            sendMessage("No region for resource " + t + " is present.");
            return;
        }

        Card best = null;
        int bestVal = Integer.MAX_VALUE;
        for (Card r : regs) {
            int v = Math.max(0, Math.min(3, r.getRegionProduction()));
            if (v < bestVal) {
                bestVal = v;
                best = r;
            }
        }
        if (best != null && best.getRegionProduction() < 3) {
            best.setRegionProduction(best.getRegionProduction() + 1);
        } else {
            sendMessage("No storage space on any " + regionName + " (already 3/3).");
        }
    }

    public boolean removeResource(String type, int n) {
        return principality.removeResource(type, n);
    }

    public void setResourceCount(String type, int n) {
        principality.setResourceCount(type, n);
    }

    // ------------- Hand -------------
    public int handSize() {
        return hand.handSize();
    }

    public void addToHand(Card c) {
        hand.addToHand(c);
    }

    public Card removeFromHandByName(String nm) {
        return hand.removeFromHandByName(nm);
    }

    // ------------- Cheap prompts used by Server -------------
    public String chooseResource() {
        sendMessage("PROMPT: Choose resource:");
        return receiveMessage();
    }

    public void refundCost(String cost) {
        if (cost == null || cost.isBlank()) {
            return;
        }
        Map<String, Integer> need = parseCost(cost);
        for (var e : need.entrySet()) {
            setResourceCount(e.getKey(), getResourceCount(e.getKey()) + e.getValue());
        }
    }

    public Map<String, Integer> parseCost(String cost) {
        Map<String, Integer> m = new HashMap<>();
        if (cost == null) {
            return m;
        }

        // Accept strings like "LW", "AA", with optional spaces or separators ("L,W", "A
        // A")
        for (int i = 0; i < cost.length(); i++) {
            char ch = cost.charAt(i);
            if (Character.isWhitespace(ch) || ch == ',' || ch == ';' || ch == '+') {
                continue;
            }
            String res = letterToResource(ch);
            if (res != null) {
                m.put(res, m.getOrDefault(res, 0) + 1);
            }
            // else: silently ignore unknown chars
        }
        return m;
    }

    public boolean payCost(String cost) {
        if (cost == null || cost.isBlank()) {
            return true;
        }
        // Cost like "1 Brick, 1 Grain, 1 Wool, 1 Lumber" etc.
        Map<String, Integer> need = parseCost(cost);
        for (var e : need.entrySet()) {
            if (getResourceCount(e.getKey()) < e.getValue()) {
                return false;
            }
        }
        for (var e : need.entrySet()) {
            removeResource(e.getKey(), e.getValue());
        }
        return true;
    }

    private String letterToResource(char ch) {
        switch (Character.toUpperCase(ch)) {
            case 'B':
                return "Brick";
            case 'G':
                return "Grain";
            case 'L':
                return "Lumber";
            case 'W':
                return "Wool";
            case 'O':
                return "Ore";
            case 'A':
                return "Gold";
            default:
                return null; // unknown / ignore
        }
    }


    public boolean hasStrengthAdvantage(Player other) {
        return this.points.strengthPoints >= 3 && this.points.strengthPoints > other.points.strengthPoints;
    }

}
