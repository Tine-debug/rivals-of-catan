package Turns.Cardevents;

import Player.Player;
import Card.Card;

public class yearOfThePlentyEvent implements CardEvent {

    @Override
    public void resolve(Player active, Player other) {

        for (int j = 0; j < 2; j++) {
            Player p = j == 0 ? active : other;
            int added = 0;
            for (int r = 0; r < p.principality.principality.size(); r++) {
                var row = p.principality.principality.get(r);
                for (int c = 0; c < row.size(); c++) {
                    Card reg = row.get(c);
                    if (reg == null || !"Region".equalsIgnoreCase(reg.getType())) {
                        continue;
                    }

                    int adj = countAdjStorehouseAbbey(p, r, c);
                    while (adj-- > 0) {
                        if (reg.getRegionProduction() < 3) {
                            reg.setRegionProduction(reg.getRegionProduction()+1);
                            added++;
                        }
                    }
                }
            }
            p.sendMessage("Year of Plenty: resources were added to your regions where adjacent to Storehouse/Abbey.");
        }
    }

    private int countAdjStorehouseAbbey(Player p, int rr, int cc) {
        int cnt = 0;
        Card up = p.getCard(rr - 1, cc);
        Card down = p.getCard(rr + 1, cc);
        if (up != null && up.toString() != null) {
            String n = up.toString().toLowerCase();
            if (n.equals("storehouse") || n.equals("abbey")) {
                cnt++;
            }
        }
        if (down != null && down.toString() != null) {
            String n = down.toString().toLowerCase();
            if (n.equals("storehouse") || n.equals("abbey")) {
                cnt++;
            }
        }
        return cnt;
    }

}
