public class PlacementFactory{
    public static Placement createPlacement(String placement){
        if (placement.equals("Center Card")) return new CenterCardPlacement();
        return new Placement(placement);
    }

}