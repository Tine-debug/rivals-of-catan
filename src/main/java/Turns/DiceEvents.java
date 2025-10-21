package Turns;

import Player.Player;
import Player.Broadcast;
import Card.Cardstacks;
import Card.Card;
import Turns.Cardevents.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiceEvents {

    private static final int EV_BRIGAND = 1;
    private static final int EV_TRADE = 2;
    private static final int EV_CELEB = 3;
    private static final int EV_PLENTY = 4;
    private static final int EV_EVENT_A = 5;
    private static final int EV_EVENT_B = 6;

    private static final Broadcast broadcast = Broadcast.getInstance();
    private static final Cardstacks stacks = Cardstacks.getInstance();

    public static int getBrigandFace(){
        return EV_BRIGAND;
    }

    public static void resolveEvent(int face, Player active, Player other) {
        List<Player> players = new ArrayList<>();
        players.add(active);
        players.add(other);
        switch (face) {
            case EV_BRIGAND:
                broadcast.broadcast("[Event] Brigand Attack");
                for (Player p : players) {
                    int total = countGoldAndWool(p, true);
                    if (total > 7) {
                        zeroGoldAndWool(p, true);
                        p.sendMessage("Brigands! You lose all Gold & Wool in affected regions.");
                    }
                }
                break;

            case EV_TRADE:
                broadcast.broadcast("[Event] Trade");
                for (Player p : players) {
                    if (p.points.commercePoints >= 3) {
                        p.sendMessage(
                                "PROMPT: Trade Advantage - gain 1 resource of your choice [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                        p.gainResource(p.receiveMessage());
                    }
                }
                break;

            case EV_CELEB:
                broadcast.broadcast("[Event] Celebration");
                int aSP = players.get(0).points.skillPoints;
                int bSP = players.get(1).points.skillPoints;
                if (aSP == bSP) {
                    for (Player p : players) {
                        p.sendMessage(
                                "PROMPT: Celebration - gain 1 resource of your choice [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                        p.gainResource(p.receiveMessage());
                    }
                } else {
                    Player winner = aSP > bSP ? players.get(0) : players.get(1);
                    winner.sendMessage(
                            "PROMPT: Celebration (you have most skill) - gain 1 resource of your choice [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    winner.gainResource(winner.receiveMessage());
                }
                break;

            case EV_PLENTY:
                broadcast.broadcast("[Event] Plentiful Harvest: each player gains 1 of choice.");
                for (Player p : players) {
                    p.sendMessage("PROMPT: Plentiful Harvest - choose a resource [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    p.gainResource(p.receiveMessage());
                    if (p.getFlags().contains("TOLLB")) {
                        int add = grantGoldIfSpace(p, 2);
                        if (add > 0) {
                            p.sendMessage("Toll Bridge: +" + add + " Gold");
                        }
                    }
                }
                break;

            case EV_EVENT_A:
            case EV_EVENT_B: {
                broadcast.broadcast("[Event] Draw Event Card");

                Card top = stacks.drawEventCard();
                if (top == null) {
                    broadcast.broadcast("Event deck empty.");
                    break;
                }
                broadcast.broadcast("EVENT: " + (top.getCardText() != null ? top.getCardText() : top.toString()));

                String nm = (top.toString() == null ? "" : top.toString()).toLowerCase();

                CardEvent event = CardEventFactory.createCardEvent(nm);
                event.resolve(active, other);

                break;
            }
            default:
                broadcast.broadcast("[Event] Unknown face " + face);
        }
    }

    private static int countGoldAndWool(Player p, boolean excludeStorehouseAdj) {
        int total = 0;
        Set<String> excluded = excludeStorehouseAdj ? storehouseExcludedKeys(p) : Set.of();
        for (int r = 0; r < p.getPrincipality().principality.size(); r++) {
            java.util.List<Card> row = p.getPrincipality().principality.get(r);
            if (row == null) {
                continue;
            }
            for (int c = 0; c < row.size(); c++) {
                Card card = row.get(c);
                if (card == null) {
                    continue;
                }
                String key = r + ":" + c;
                if (excluded.contains(key)) {
                    continue;
                }
                if ("Gold Field".equalsIgnoreCase(card.toString()) || "Pasture".equalsIgnoreCase(card.toString())) {
                    total += Math.max(0, Math.min(3, card.getRegionProduction()));
                }
            }

        }
        return total;
    }

    private static Set<String> storehouseExcludedKeys(Player p) {
        Set<String> out = new HashSet<>();
        for (int r = 0; r < p.getPrincipality().principality.size(); r++) {
            List<Card> row = p.getPrincipality().principality.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x != null && x.toString() != null && x.toString().equalsIgnoreCase("Storehouse")) {
                    // Decide side: if there’s a settlement/city below we’re on upper side, else
                    // lower
                    boolean belowCenter = nmAt(p.getCard(r + 1, c), "Settlement", "City")
                            || nmAt(p.getCard(r + 2, c), "City", "City");
                    int regionRow = belowCenter ? r - 1 : r + 1;
                    out.add(regionRow + ":" + (c - 1));
                    out.add(regionRow + ":" + (c + 1));
                }
            }
        }
        return out;
    }

    private static int grantGoldIfSpace(Player p, int want) {
        int given = 0;
        for (int r = 0; r < p.getPrincipality().principality.size(); r++) {
            java.util.List<Card> row = p.getPrincipality().principality.get(r);
            if (row == null) {
                continue;
            }
            for (int c = 0; c < row.size(); c++) {
                if (given >= want) {
                    break; // stop if already satisfied

                }
                Card card = row.get(c);
                if (card != null && "Gold Field".equalsIgnoreCase(card.toString())) {
                    int can = Math.max(0, 3 - card.getRegionProduction());
                    int add = Math.min(can, want - given);
                    if (add > 0) {
                        card.setRegionProduction(card.getRegionProduction()+add);
                        given += add;
                    }
                }
            }
        }
        return given;
    }

    private static boolean nmAt(Card c, String a, String b) {
        if (c == null || c.toString() == null) {
            return false;
        }
        return c.toString().equalsIgnoreCase(a) || c.toString().equalsIgnoreCase(b);
    }

    private static void zeroGoldAndWool(Player p, boolean excludeStorehouseAdj) {
        Set<String> excluded = excludeStorehouseAdj ? storehouseExcludedKeys(p) : Set.of();
        forEachRegion(p, (r, c, card) -> {
            if (card == null) {
                return;
            }
            String key = r + ":" + c;
            if (excluded.contains(key)) {
                return;
            }
            if ("Gold Field".equalsIgnoreCase(card.toString()) || "Pasture".equalsIgnoreCase(card.toString())) {
                card.setRegionProduction(0);
            }
        });
    }

    private interface RegionVisitor {

        void visit(int r, int c, Card card);
    }

    private static void forEachRegion(Player p, RegionVisitor v) {
        for (int r = 0; r < p.getPrincipality().principality.size(); r++) {
            List<Card> row = p.getPrincipality().principality.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card card = row.get(c);
                if (card != null && "Region".equalsIgnoreCase(card.getType())) {
                    v.visit(r, c, card);
                }
            }
        }
    }

}
