
public class LogicFactory{
    public static Logic createLogic(String placement, String name, String type){
        if (type.equals("Region")) return new RegionLogic();
        if (placement.equals("Settlement/city")) return new SettlementExpansionLogic();
        if (placement.equals("Action")) return new ActionLogic();
        if (name.equals("City")) return new CityLogic();
        if (name.equals("Settlement")) return new SettlementLogic();
        if (name.equals("Road")) return new RoadLogic();
        return new defaultLogic();
    }
}