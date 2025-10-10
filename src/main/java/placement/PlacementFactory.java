public class PlacementFactory{
    public static Placement createPlacement(String placement){
        if (placement.equals("Center Card")) return new CenterCardPlacement();
        if(placement.equals("Region")) return new RegionPlacement();
        return new Placement(placement);
    }

}