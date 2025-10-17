
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Player {

    public Points points = new Points("2", null, null, null, null, null, null);

    public int tradeRate = 3; // default 3:1 with bank
    public boolean isBot = false;

    public Set<String> flags = new HashSet<>();

    public Map<String, Integer> resources = new HashMap<>();

    public List<Card> hand = new ArrayList<>();

    public List<List<Card>> principality = new ArrayList<>();

    public int lastSettlementRow = -1, lastSettlementCol = -1;

    private final Scanner in = new Scanner(System.in);

    public Player() {
        String[] all = { "Brick", "Grain", "Lumber", "Wool", "Ore", "Gold", "Any" };
        principality = new java.util.ArrayList<>();
        for (int r = 0; r < 5; r++) {
            java.util.List<Card> row = new java.util.ArrayList<>();
            for (int c = 0; c < 5; c++)
                row.add(null);
            principality.add(row);
        }
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
        if (r < 0 || c < 0)
            return null;
        if (r >= principality.size())
            return null;
        List<Card> row = principality.get(r);
        if (row == null || c >= row.size())
            return null;
        return row.get(c);
    }

    public void placeCard(int r, int c, Card card) {
        ensureSize(r, c);
        principality.get(r).set(c, card);
    }


    public int expandAfterEdgeBuild(int col) {
        int cols = principality.get(0).size();
        if (col == 0) {
            for (java.util.List<Card> row : principality) {
                row.add(0, null);
            }
            col += 1;
            if (lastSettlementCol >= 0)
                lastSettlementCol += 1;
        } else if (col == cols - 1) {
            for (java.util.List<Card> row : principality) {
                row.add(null);
            }
        }
        return col;
    }

    private void ensureSize(int r, int c) {
        while (principality.size() <= r) {
            ArrayList<Card> row = new ArrayList<>();
            int cols = principality.isEmpty() ? 5 : principality.get(0).size();
            for (int i = 0; i < cols; i++)
                row.add(null);
            principality.add(row);
        }
        for (List<Card> row : principality) {
            while (row.size() <= c)
                row.add(null);
        }
    }

    public boolean hasInPrincipality(String name) {
        for (List<Card> row : principality)
            for (Card c : row)
                if (c != null && c.name != null && c.name.equalsIgnoreCase(name))
                    return true;
        return false;
    }

    public String printPrincipality() {
        StringBuilder sb = new StringBuilder();
        int rows = principality.size();
        int cols = principality.isEmpty() ? 0 : principality.get(0).size();

        int[] w = new int[cols];
        int minW = 10; 
        for (int c = 0; c < cols; c++) {
            int m = minW;
            for (int r = 0; r < rows; r++) {
                Card card = getCard(r, c);
                String title = cellTitle(card);
                String info = cellInfo(card);
                m = Math.max(m, title.length());
                m = Math.max(m, info.length());
            }
            w[c] = m;
        }

        sb.append("      "); 
        for (int c = 0; c < cols; c++) {
            String hdr = "Col " + c;
            sb.append(padRight(hdr, w[c] + 3));
        }
        sb.append("\n");

        sb.append("    ").append(buildSep(w)).append("\n");

        for (int r = 0; r < rows; r++) {
            sb.append(String.format("%2d  ", r)); 
            sb.append("|");
            for (int c = 0; c < cols; c++) {
                String title = cellTitle(getCard(r, c));
                sb.append(" ").append(padRight(title, w[c])).append(" ").append("|");
            }
            sb.append("\n");

            sb.append("    ");
            sb.append("|");
            for (int c = 0; c < cols; c++) {
                String info = cellInfo(getCard(r, c));
                sb.append(" ").append(padRight(info, w[c])).append(" ").append("|");
            }
            sb.append("\n");

            sb.append("    ").append(buildSep(w)).append("\n");
        }

        sb.append("\nPoints: ")
                .append("VP=").append(points.victoryPoints)
                .append("  CP=").append(points.commercePoints)
                .append("  SP=").append(points.skillPoints)
                .append("  FP=").append(points.strengthPoints)
                .append("  PP=").append(points.progressPoints)
                .append("\n");

        return sb.toString();
    }


    public String printHand() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hand (").append(hand.size()).append("):\n");
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c == null)
                continue;
            String cost = (c.cost == null || c.cost.isBlank()) ? "-" : c.cost;
            String pts = summarizePoints(c);
            sb.append("  [").append(i).append("] ")
                    .append(c.name == null ? "Unknown" : c.name)
                    .append("   {cost: ").append(cost).append("} ")
                    .append(pts.isEmpty() ? "" : pts)
                    .append("\n").append(c.cardText == null ? "" : "\t" + c.cardText + "\n");
        }
        return sb.toString();
    }

  
    private String buildSep(int[] w) {
        StringBuilder sep = new StringBuilder();
        sep.append("+");
        for (int c = 0; c < w.length; c++) {
            sep.append("-".repeat(w[c] + 2)); 
            sep.append("+");
        }
        return sep.toString();
    }

    
    private String padRight(String s, int w) {
        if (s == null)
            s = "";
        if (s.length() >= w)
            return s;
        return s + " ".repeat(w - s.length());
    }

    // ---------- helpers used by printPrincipality ----------

    private String cellTitle(Card c) {
        if (c == null)
            return "";
        String title = c.name;
        if (title.equals("Forest"))
            title += " (L):Lumber";
        else if (title.equals("Hill"))
            title += " (B):Brick";
        else if (title.equals("Field"))
            title += " (G):Grain";
        else if (title.equals("Pasture"))
            title += " (W):Wool";
        else if (title.equals("Mountain"))
            title += " (O):Ore";
        else if (title.equals("Gold Field"))
            title += " (A):Gold";
        return title == null ? "Unknown" : title;
    }

    private String cellInfo(Card c) {
        if (c == null)
            return "";
        if ("Region".equalsIgnoreCase(c.type)) {
            String die = (c.diceRoll <= 0 ? "-" : String.valueOf(c.diceRoll));
            int stored = Math.max(0, Math.min(3, c.regionProduction));
            return "d" + die + "  " + stored + "/3";
        }

        String nm = c.name == null ? "" : c.name;
        if (c.type != null && c.type.toLowerCase().contains("trade ship")) {
            if (!nm.equalsIgnoreCase("Large Trade Ship") && nm.endsWith("Ship")) {
                String res = firstWord(nm); // Brick / Grain / etc.
                return "2:1 " + res;
            } else if (nm.equalsIgnoreCase("Large Trade Ship")) {
                return "LTS (left/right swap 2→1)";
            }
        }

        if ("Building".equalsIgnoreCase(c.type) &&
                "Settlement/City Expansions".equalsIgnoreCase(c.placement)) {
            if (nm.endsWith("Foundry"))
                return "Boosts Ore x2 on match";
            if (nm.endsWith("Mill"))
                return "Boosts Grain x2 on match";
            if (nm.endsWith("Camp"))
                return "Boosts Lumber x2 on match";
            if (nm.endsWith("Factory"))
                return "Boosts Brick x2 on match";
            if (nm.endsWith("Shop"))
                return "Boosts Wool x2 on match";
        }

        if ("Road".equalsIgnoreCase(nm))
            return "Center";
        if ("Settlement".equalsIgnoreCase(nm))
            return "Center";
        if ("City".equalsIgnoreCase(nm))
            return "Center";

        String pts = summarizePoints(c);
        if (!pts.isEmpty())
            return pts;

        String pl = c.placement == null ? "" : c.placement;
        String tp = c.type == null ? "" : c.type;
        if (!pl.isEmpty() || !tp.isEmpty())
            return (pl + " " + tp).trim();
        return "";
    }

    // SCORING helper: summarize points on a card like "[VP1 CP2 SP1 FP0 PP0]"
    private String summarizePoints(Card c) {
        StringBuilder t = new StringBuilder();
            if (c.points.victoryPoints > 0 || c.points.commercePoints > 0 || c.points.progressPoints > 0 || c.points.strengthPoints > 0 ||
            c.points.skillPoints >0){
            t.append("[");
            if (c.points.victoryPoints > 0)
                t.append("VP").append(c.points.victoryPoints).append(" ");
            if (c.points.commercePoints > 0)
                t.append("CP").append(c.points.commercePoints).append(" ");
            if (c.points.strengthPoints > 0)
                t.append("SP").append(c.points.strengthPoints).append(" ");
            if (c.points.skillPoints > 0)
                t.append("FP").append(c.points.skillPoints).append(" ");
            if (c.points.progressPoints> 0)
                t.append("PP").append(c.points.progressPoints).append(" ");
            if (t.charAt(t.length() - 1) == ' ')
                t.deleteCharAt(t.length() - 1);
            t.append("]");
            }
        return t.toString();
    }

    // Advantage tokens depend on being >= 3 ahead of the opponent.
    public boolean hasTradeTokenAgainst(Player opp) {
        return (this.points.commercePoints - (opp == null ? 0 : opp.points.commercePoints)) >= 3;
    }

    public boolean hasStrengthTokenAgainst(Player opp) {
        return (this.points.strengthPoints - (opp == null ? 0 : opp.points.strengthPoints)) >= 3;
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

    private String firstWord(String s) {
        if (s == null)
            return "";
        String[] toks = s.trim().split("\\s+");
        return toks.length == 0 ? "" : toks[0];
    }

    private int parseIntSafe(String s) {
        if (s == null || s.isBlank())
            return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
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

    // Collect all Region cards of a given region-name (e.g., "Forest")
    private java.util.List<Card> findRegions(String regionName) {
        java.util.List<Card> list = new java.util.ArrayList<>();
        if (regionName == null)
            return list;
        for (int r = 0; r < principality.size(); r++) {
            java.util.List<Card> row = principality.get(r);
            if (row == null)
                continue;
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x != null &&
                        "Region".equalsIgnoreCase(x.type) &&
                        x.name != null &&
                        x.name.equalsIgnoreCase(regionName)) {
                    list.add(x);
                }
            }
        }
        return list;
    }

    // Sum stored resources on all regions (of ANY type)
    public int totalAllResources() {
        int sum = 0;
        for (int r = 0; r < principality.size(); r++) {
            java.util.List<Card> row = principality.get(r);
            if (row == null)
                continue;
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x != null && "Region".equalsIgnoreCase(x.type)) {
                    sum += Math.max(0, Math.min(3, x.regionProduction));
                }
            }
        }
        return sum;
    }

    // Count stored resources of a specific resource type across the board
    public int getResourceCount(String type) {
        String regionName = resourceToRegion(type);
        if (regionName == null)
            return 0;
        if ("Any".equals(regionName))
            return totalAllResources();
        int sum = 0;
        for (Card r : findRegions(regionName)) {
            sum += Math.max(0, Math.min(3, r.regionProduction));
        }
        return sum;
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

        java.util.List<Card> regs = findRegions(regionName);
        if (regs.isEmpty()) {
            sendMessage("No region for resource " + t + " is present.");
            return;
        }

        // pick lowest stored (<3); tie -> first in board order
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
            // Optional: feedback
            // sendMessage("Gained 1 " + t + " on " + regionName + " (" +
            // (best.regionProduction) + "/3)");
        } else {
            sendMessage("No storage space on any " + regionName + " (already 3/3).");
        }
    }

    // Remove N resources of a type: repeatedly remove from the region with the
    // HIGHEST stock (>0)
    // Returns true if all could be removed, false otherwise (removes as many as
    // possible).
    public boolean removeResource(String type, int n) {
        if (n <= 0)
            return true;
        String regionName = resourceToRegion(type);
        if (regionName == null || "Any".equals(regionName))
            return false;

        java.util.List<Card> regs = findRegions(regionName);
        if (regs.isEmpty())
            return false;

        int removed = 0;
        while (removed < n) {
            // find highest stocked region (>0)
            Card best = null;
            int bestVal = -1;
            for (Card r : regs) {
                int v = Math.max(0, Math.min(3, r.regionProduction));
                if (v > bestVal) {
                    bestVal = v;
                    best = r;
                }
            }
            if (best == null || bestVal <= 0)
                break; // no more to remove
            best.regionProduction -= 1;
            removed++;
        }
        return removed == n;
    }

    // Set total stored resources for a type by redistributing across its regions.
    // If n <= 0, zero all matching regions.
    // If increasing, fill lowest first; if decreasing, remove from highest first.
    public void setResourceCount(String type, int n) {
        String regionName = resourceToRegion(type);
        if (regionName == null || "Any".equals(regionName))
            return;

        java.util.List<Card> regs = findRegions(regionName);
        if (regs.isEmpty())
            return;

        // clamp desired total between 0 and regions*3
        int maxTotal = regs.size() * 3;
        int want = Math.max(0, Math.min(maxTotal, n));

        // current total
        int cur = 0;
        for (Card r : regs) {
            r.regionProduction = Math.max(0, Math.min(3, r.regionProduction)); // sanitize
            cur += r.regionProduction;
        }
        if (cur == want)
            return;

        if (cur < want) {
            // add (want - cur) by filling lowest first
            int need = want - cur;
            while (need > 0) {
                Card best = null;
                int bestVal = Integer.MAX_VALUE;
                for (Card r : regs) {
                    int v = r.regionProduction;
                    if (v < 3 && v < bestVal) {
                        bestVal = v;
                        best = r;
                    }
                }
                if (best == null || best.regionProduction >= 3)
                    break;
                best.regionProduction += 1;
                need--;
            }
        } else {
            // remove (cur - want) by draining highest first
            int drop = cur - want;
            while (drop > 0) {
                Card best = null;
                int bestVal = -1;
                for (Card r : regs) {
                    int v = r.regionProduction;
                    if (v > bestVal) {
                        bestVal = v;
                        best = r;
                    }
                }
                if (best == null || best.regionProduction <= 0)
                    break;
                best.regionProduction -= 1;
                drop--;
            }
        }
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