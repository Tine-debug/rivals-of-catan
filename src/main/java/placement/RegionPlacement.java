
public class RegionPlacement extends  Placement{
    public RegionPlacement(){
        this.placement = "Region";
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card){
            if (row == 2 || row == 4 || row == 0) {
                active.sendMessage("Regions must be placed above/below the center row.");
                return false;
            }
            if (card.regionProduction < 0) card.regionProduction = 0;
            active.placeCard(row, col, card);
            return true;
    }


}