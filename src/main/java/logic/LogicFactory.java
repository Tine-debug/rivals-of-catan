
public class LogicFactory{
    public static Logic createLogic(String placement, String name, String type){
        if (name.equals("City")) return new CityLogic();
        if (name.equals("Settlement")) return new SettlementLogic();
        if (placement.equals("Center Card")) return new CenterCardLogic();
        if(placement.equals("Region")) return new RegionLogic();
        if (placement.equals("Settlement/city")) return new SettlementExpansionLogic();
        if (placement.equals("Action")) return new ActionLogic();
        return new Logic();
    }
}