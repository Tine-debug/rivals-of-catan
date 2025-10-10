
public class RegionPlacement extends  Placement{
    public RegionPlacement(){
        this.placement = "Region";
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card){

            String nm = (card.name == null ? "" : card.name);
            System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");

            if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
            }
            if (row == 2 || row == 4 || row == 0) {
                active.sendMessage("Regions must be placed above/below the center row.");
                return false;
            }
            if (card.regionProduction < 0) card.regionProduction = 0;
            active.placeCard(row, col, card);
            return true;
    }


}