


public class Logic{

    public Logic(){}

    
   public boolean applyEffect(Player active, Player other, int row, int col, Card card) {
        String nm = (card.name == null ? "" : card.name);
        System.out.println("ApplyEffect: " + nm + " at (" + row + "," + col + ")");


        if (active.getCard(row, col) != null) {
            active.sendMessage("That space is occupied.");
            return false;
        }

  
        // Fallback: accept placement (ugly default)
        active.placeCard(row, col, card);
        return true;
    }

    



}