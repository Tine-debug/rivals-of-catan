
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Player {

    public Points points = new Points("2", null, null, null, null, null, null);

    public int tradeRate = 3; 
    public boolean isBot = false;

    public Set<String> flags = new HashSet<>();

    public Map<String, Integer> resources = new HashMap<>();

    public List<Card> hand = new ArrayList<>();

    public Principality principality;

    public int lastSettlementRow = -1, lastSettlementCol = -1;

    private final Scanner in = new Scanner(System.in);

    public Player() {
        String[] all = { "Brick", "Grain", "Lumber", "Wool", "Ore", "Gold", "Any" };
        principality = new Principality();
        resources = new java.util.HashMap<>();
        for (String r : all)
            resources.put(r, 0);
    }

    public void sendMessage(Object m) {
        System.out.println(m);
    }

    public String receiveMessage() {
        System.out.print("> ");
        return in.nextLine();
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
        StringBuilder sb = new StringBuilder();
        sb.append("Hand (").append(hand.size()).append("):\n");
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c == null)
                continue;
            String cost = (c.cost == null || c.cost.isBlank()) ? "-" : c.cost;
            String pts = c.summarizePoints();
            sb.append("  [").append(i).append("] ")
                    .append(c.name == null ? "Unknown" : c.name)
                    .append("   {cost: ").append(cost).append("} ")
                    .append(pts.isEmpty() ? "" : pts)
                    .append("\n").append(c.cardText == null ? "" : "\t" + c.cardText + "\n");
        }
        return sb.toString();
    }

  
    public boolean hasTradeTokenAgainst(Player opp) {
        if (this.points.commercePoints < 3) return false;
        if (opp == null) return true;
        return (this.points.commercePoints > opp.points.commercePoints);
    }

    public boolean hasStrengthTokenAgainst(Player opp) {
        if (this.points.strengthPoints < 3) return false;
        if (opp == null) return true;
        return (this.points.strengthPoints > opp.points.strengthPoints);
    }

    // Final score used for win check: base VP + 1 per advantage token against
    // opponent
    public int currentScoreAgainst(Player opp) {
        int score = this.points.victoryPoints;
        if (hasTradeTokenAgainst(opp))
            score += 1;
        if (hasStrengthTokenAgainst(opp))
            score += 1;
        return score;
    }


    // ------------- Resources (per-region, not pooled) -------------

    // Map a resource name to its Region card name
    private String resourceToRegion(String type) {
        if (type == null)
            return null;
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
            int v = Math.max(0, Math.min(3, r.regionProduction));
            if (v < bestVal) {
                bestVal = v;
                best = r;
            }
        }
        if (best != null && best.regionProduction < 3) {
            best.regionProduction += 1;
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
        return hand.size();
    }

    public void addToHand(Card c) {
        hand.add(c);
    }

    public Card removeFromHandByName(String nm) {
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c != null && c.name != null && c.name.equalsIgnoreCase(nm)) {
                return hand.remove(i);
            }
        }
        return null;
    }

    // ------------- Cheap prompts used by Server -------------
    public String chooseResource() {
        sendMessage("PROMPT: Choose resource:");
        return receiveMessage();
    }
}