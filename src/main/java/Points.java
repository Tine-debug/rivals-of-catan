public class Points{

int skillPoints = 0;
int strengthPoints = 0;
int victoryPoints = 0;
int progressPoints= 0;
int commercePoints = 0;
int sailPoints = 0;
int canonPoints = 0;

public Points(){

}

//String victoryPoints, String CP, String SP, String FP, String PP, String LP, String KP
public Points(String victoryPoints, String CP, String SP, String FP, String PP, String LP, String KP){
    this.victoryPoints = asInt(victoryPoints, 0);
    this.commercePoints = asInt(CP, 0);
    this.strengthPoints = asInt(SP, 0);
    this.skillPoints = asInt(FP, 0);
    this.sailPoints = asInt(LP, 0);
    this.canonPoints = asInt(KP, 0);

}



public Points addPoints(Points points1, Points points2){
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
            if (s == null)
                return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
}