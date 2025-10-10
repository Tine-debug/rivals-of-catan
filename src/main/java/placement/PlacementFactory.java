public class PlacementFactory{
    public static Placement createPlacement(String placement){
        if (placement.equals("Center Card")) return new CenterCardPlacement();
        if(placement.equals("Region")) return new RegionPlacement();
        if (placement.equals("Settlement/city")) return new SettlementExpansionPlacement();
        if (placement.equals("Action")) return new ActionPlacement();
        return new Placement(placement);
    }

}