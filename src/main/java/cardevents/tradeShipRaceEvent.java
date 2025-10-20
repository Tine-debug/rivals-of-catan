
public class tradeShipRaceEvent implements CardEvent {

    private final Broadcast broadcast = Broadcast.getInstance();
    @Override
    public void resolve(Player active, Player other) {
        int c0 = countTradeShips(active);
        int c1 = countTradeShips(other);
        Player p;

        if (c0 == 0 && c1 == 0) {
            broadcast.broadcast("Trade Ships Race: no one owns trade ships.");
            return;
        }
        if (c0 > c1) {
            p = active;
            p.sendMessage(
                    "PROMPT: Trade Ships Race - you have the most trade ships. Choose 1 resource [Brick|Grain|Lumber|Wool|Ore|Gold]:");
            p.gainResource(p.receiveMessage());
        } else if (c1 > c0) {
            p = other;
            p.sendMessage(
                    "PROMPT: Trade Ships Race - you have the most trade ships. Choose 1 resource [Brick|Grain|Lumber|Wool|Ore|Gold]:");
            p.gainResource(p.receiveMessage());
        } else { // tie
            if (c0 >= 1 && c1 >= 1) {
                for (int j = 0; j < 2; j++) {
                    p = j == 0 ? active : other;
                    p.sendMessage(
                            "PROMPT: Trade Ships Race (tie) - choose 1 resource [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    p.gainResource(p.receiveMessage());
                }
            } else {
                broadcast.broadcast("Trade Ships Race: tie without both having ≥1 ship; no one receives a resource.");
            }
        }
    }

    private int countTradeShips(Player p) {
        int count = 0;
        for (int r = 0; r < p.principality.principality.size(); r++) {
            var row = p.principality.principality.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x == null) {
                    continue;
                }
                String t = x.type == null ? "" : x.type;
                String pl = x.placement == null ? "" : x.placement;
                if (t.toLowerCase().contains("trade ship")
                        || (pl.toLowerCase().contains("settlement/city") && x.name != null
                        && x.name.toLowerCase().endsWith("ship"))) {
                    count++;
                }
            }
        }
        return count;
    }

}
