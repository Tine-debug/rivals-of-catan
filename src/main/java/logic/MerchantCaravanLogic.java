public class MerchantCaravanLogic implements Logic{

    public MerchantCaravanLogic() {
    }

    @Override
    public boolean applyEffect(Player active, Player other, int row, int col, Card card) {
        if (active.totalAllResources() < 2) {
                    active.sendMessage("You need at least 2 resources to play Merchant Caravan.");
                    return false;
                }
                for (int i = 0; i < 2; i++) {
                    active.sendMessage(
                            "PROMPT: Type Discard resource #" + (i + 1) + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    String g = active.receiveMessage();
                    active.removeResource(g, 1);
                }
                for (int i = 0; i < 2; i++) {
                    active.sendMessage(
                            "PROMPT: Type Gain resource #" + (i + 1) + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    String g = active.receiveMessage();
                    active.gainResource(g);
                }
        return true;
    }



}