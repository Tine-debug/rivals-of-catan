
public class Points {

    public int skillPoints = 0;
    public int strengthPoints = 0;
    public int victoryPoints = 0;
    public int progressPoints = 0;
    public int commercePoints = 0;
    public int sailPoints = 0;
    public int canonPoints = 0;

    public Points() {

    }

//String victoryPoints, String CP, String SP, String FP, String PP, String LP, String KP
    public Points(String victoryPoints, String CP, String SP, String FP, String PP, String LP, String KP) {
        this.victoryPoints = asInt(victoryPoints, 0);
        this.commercePoints = asInt(CP, 0);
        this.strengthPoints = asInt(SP, 0);
        this.skillPoints = asInt(FP, 0);
        this.sailPoints = asInt(LP, 0);
        this.canonPoints = asInt(KP, 0);

    }

    public static Points addPoints(Points points1, Points points2) {
        Points result = new Points();

        result.victoryPoints = points1.victoryPoints + points2.victoryPoints;
        result.commercePoints = points1.commercePoints + points2.commercePoints;
        result.strengthPoints = points1.strengthPoints + points2.strengthPoints;
        result.skillPoints = points1.skillPoints + points2.skillPoints;
        result.sailPoints = points1.sailPoints + points2.sailPoints;
        result.canonPoints = points1.canonPoints + points1.canonPoints;

        return result;

    }

    private int asInt(String s, int def) {
        try {
            if (s == null) {
                return def;
            }
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    public String summarizePoints() {
        StringBuilder t = new StringBuilder();
        if (victoryPoints > 0 || commercePoints > 0 || progressPoints > 0 || strengthPoints > 0
                || skillPoints > 0) {
            t.append("[");
            if (victoryPoints > 0) {
                t.append("VP").append(victoryPoints).append(" ");
            }
            if (commercePoints > 0) {
                t.append("CP").append(commercePoints).append(" ");
            }
            if (strengthPoints > 0) {
                t.append("SP").append(strengthPoints).append(" ");
            }
            if (skillPoints > 0) {
                t.append("FP").append(skillPoints).append(" ");
            }
            if (progressPoints > 0) {
                t.append("PP").append(progressPoints).append(" ");
            }
            if (t.charAt(t.length() - 1) == ' ') {
                t.deleteCharAt(t.length() - 1);
            }
            t.append("]");
        }
        return t.toString();
    }
}
