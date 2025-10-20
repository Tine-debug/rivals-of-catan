
public class travelingMerchantEvent implements Event {

    @Override
    public void resolve(Player active, Player other) {
        for (int j = 0; j < 2; j++) {
            Player p = j == 0 ? active : other;
            int max = Math.min(2, p.getResourceCount("Gold"));
            if (max <= 0) {
                p.sendMessage("Traveling Merchant: not enough Gold to trade (need 1 per resource).");
                continue;
            }
            p.sendMessage(
                    "PROMPT: Traveling Merchant - you may take up to " + max + " resources (1 Gold each). How many (0.."
                    + max + ")?");
            int k = 0;
            try {
                k = Integer.parseInt(p.receiveMessage().trim());
            } catch (Exception ignored) {
            }
            if (k < 0) {
                k = 0;
            }
            if (k > max) {
                k = max;
            }

            for (int i = 0; i < k; i++) {
                p.sendMessage("PROMPT: Pick resource #" + (i + 1) + ":");
                String res = p.receiveMessage();
                if (p.removeResource("Gold", 1)) {
                    p.gainResource(res);
                } else {
                    p.sendMessage("No more Gold; stopping.");
                    break;
                }
            }
        }

    }

}
