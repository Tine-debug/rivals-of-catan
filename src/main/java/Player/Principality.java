package Player;

import Card.Card;
import java.util.ArrayList;
import java.util.List;

public class Principality{

    public Principality() {
        principality = new java.util.ArrayList<>();
        for (int r = 0; r < 5; r++) {
            java.util.List<Card> row = new java.util.ArrayList<>();
            for (int c = 0; c < 5; c++)
                row.add(null);
            principality.add(row);
        }
    }



    public List<List<Card>> principality = new ArrayList<>();
    public int lastSettlementRow = -1, lastSettlementCol = -1;

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

    public String printPrincipality(Player player) {
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
                .append("VP=").append(player.points.victoryPoints)
                .append("  CP=").append(player.points.commercePoints)
                .append("  SP=").append(player.points.skillPoints)
                .append("  FP=").append(player.points.strengthPoints)
                .append("  PP=").append(player.points.progressPoints)
                .append("\n");

        return sb.toString();
    }


    private String cellTitle(Card c) {
        if (c == null)
            return "";
        String title = c.name;
        switch (title) {
            case "Forest":
                title += " (L):Lumber";
                break;
            case "Hill":
                title += " (B):Brick";
                break;
            case "Field":
                title += " (G):Grain";
                break;
            case "Pasture":
                title += " (W):Wool";
                break;
            case "Mountain":
                title += " (O):Ore";
                break;
            case "Gold Field":
                title += " (A):Gold";
                break;
            default:
                break;
        }
        return title;
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

        String pts = c.summarizePoints();
        if (!pts.isEmpty())
            return pts;

        String pl = c.placement == null ? "" : c.placement;
        String tp = c.type == null ? "" : c.type;
        if (!pl.isEmpty() || !tp.isEmpty())
            return (pl + " " + tp).trim();
        return "";
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


    private String firstWord(String s) {
        if (s == null)
            return "";
        String[] toks = s.trim().split("\\s+");
        return toks.length == 0 ? "" : toks[0];
    }


    public java.util.List<Card> findRegions(String regionName) {
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

public void setResourceCount(String type, int n) {
        String regionName = resourceToRegion(type);
        if (regionName == null || "Any".equals(regionName))
            return;

        java.util.List<Card> regs = findRegions(regionName);
        if (regs.isEmpty())
            return;

        int maxTotal = regs.size() * 3;
        int want = Math.max(0, Math.min(maxTotal, n));

        int cur = 0;
        for (Card r : regs) {
            r.regionProduction = Math.max(0, Math.min(3, r.regionProduction)); 
            cur += r.regionProduction;
        }
        if (cur == want)
            return;

        if (cur < want) {
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

}