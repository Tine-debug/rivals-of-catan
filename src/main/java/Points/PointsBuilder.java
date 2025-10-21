package Points;

public class PointsBuilder {

    private int skillPoints = 0;
    private int strengthPoints = 0;
    private int victoryPoints = 0;
    private int progressPoints = 0;
    private int commercePoints = 0;
    private int sailPoints = 0;
    private int canonPoints = 0;

    private int asInt(String s, int def) {
        try {
            if (s == null) {
                return def;
            }
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public void skillPoints(int skillPoints){
        this.skillPoints = skillPoints;
    }
    public void skillPoints(String skillPoints){
        this.skillPoints = asInt(skillPoints, 0);
    }

    public void strengthPoints(int strengthPoints){
        this.strengthPoints = strengthPoints;
    }

    public void strengthPoints(String strengthPoints){
        this.strengthPoints = asInt(strengthPoints, 0);
    }

    public void victoryPoints(int victoryPoints){
        this.victoryPoints = victoryPoints;
    }

    public void victoryPoints(String victoryPoints){
        this.victoryPoints = asInt(victoryPoints, 0);
    }

    public void progressPoints(int progressPoints){
        this.progressPoints = progressPoints;
    }

    public void progressPoints(String progressPoints){
        this.progressPoints = asInt(progressPoints, 0);
    }

    public void commercePoints(int commercePoints){
        this.commercePoints = commercePoints;
    }

    public void commercePoints(String commercePoints){
        this.commercePoints = asInt(commercePoints, 0);
    }

    public void sailPoints(int sailPoints){
        this.sailPoints = sailPoints;
    }

    public void sailPoints(String sailPoints){
        this.sailPoints = asInt(sailPoints, 0);
    }

    public void canonPoints(int canonPoints){
        this.canonPoints = canonPoints;
    }

    public void canonPoints(String canonPoints){
        this.canonPoints = asInt(canonPoints, 0);
    }

    public Points build() {
        return new Points(victoryPoints, commercePoints, strengthPoints,
                skillPoints, progressPoints, sailPoints, canonPoints);
    }

}
