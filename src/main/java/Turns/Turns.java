package Turns;

import Player.Player;
import Player.Broadcast;
import Card.Cardstacks;
import Card.Card;

import Turns.Phases.*;

import java.util.List;
import java.util.Random;

public class Turns {

    public Turns(List<Player> players) {
        this.players = players;
    }

    private final Broadcast broadcast = Broadcast.getInstance();
    private final Cardstacks stacks = Cardstacks.getInstance();

    private final Random rng = new Random();
    List<Player> players;

    public int resolveOneTurn(int current, boolean random) {
        Player active = players.get(current);
        Player other = players.get((current + 1) % players.size());

        int eventFace;
        int prodFace;
        if (random) {
            eventFace = rollEventDie(active);
            prodFace = rollProductionDie(active);
        } else {
            eventFace = getNonRandomEventDie();
            prodFace = getNonRandomProductionDie(active);
        }

        if (eventFace == DiceEvents.getBrigandFace()) {
            DiceEvents.resolveEvent(eventFace, active, other);
            applyProduction(prodFace);
        } else {
            applyProduction(prodFace);
            DiceEvents.resolveEvent(eventFace, active, other);
        }

        for (int i = 0; i < players.size(); i++) {
            players.get(i).sendMessage("Opponent's board:");
            players.get(i).sendMessage(
                    "\t\t" + players.get((i + 1) % players.size()).printPrincipality().replace("\n", "\n\t\t"));
            players.get(i).sendMessage("Your board:");
            players.get(i).sendMessage(players.get(i).printPrincipality());
            players.get(i).sendMessage("Your hand:");
            players.get(i).sendMessage(players.get(i).printHand());
        }

        actionphase.resolvePhase(active, other);

        replenish(active);

        ExchangePhase.resolvePhase(active);

        if (checkWinEndOfTurn(active, other)) {
            return 3;
        }

        current = (current + 1) % players.size();

        return current;

    }

    protected int rollEventDie(Player active) {
        int face = 1 + rng.nextInt(6);
        broadcast.broadcast("[EventDie] -> " + face);
        return face;
    }

    protected int rollProductionDie(Player active) {
        int face = 1 + rng.nextInt(6);
        if (active.flags.contains("BRIGITTA")) {
            active.sendMessage("PROMPT: Brigitta active -  choose production die [1-6]:");
            try {
                int forced = Integer.parseInt(active.receiveMessage().trim());
                if (forced >= 1 && forced <= 6) {
                    face = forced;
                }
            } catch (NumberFormatException ignored) {
            }
            active.flags.remove("BRIGITTA");
        }
        broadcast.broadcast("[ProductionDie] -> " + face);
        return face;
    }

    private void applyProduction(int face) {
        for (Player p : players) {
            boolean hasMarketplace = p.flags.contains("MARKETPLACE");
            int pMatches = countFaceRegions(p, face);
            int oppMatches = countFaceRegions(opponentOf(p), face);

            for (int r = 0; r < p.principality.principality.size(); r++) {
                List<Card> row = p.principality.principality.get(r);
                for (int c = 0; c < row.size(); c++) {
                    Card card = row.get(c);
                    if (card == null || !"Region".equalsIgnoreCase(card.type)) {
                        continue;
                    }
                    if (card.diceRoll != face) {
                        continue;
                    }
                    int inc = 1;
                    if (hasAdjacentBoosterForRegion(p, r, c)) {
                        inc += 1;
                    }

                    card.regionProduction = Math.min(3, card.regionProduction + inc);
                }
            }

            if (hasMarketplace && oppMatches > pMatches) {
                p.sendMessage("PROMPT: Marketplace - choose one resource produced on face " + face
                        + " to gain (e.g., Grain/Gold/Lumber):");
                String res = p.receiveMessage();
                p.gainResource(res);
            }
        }
    }

    public void replenish(Player p) {
        if (p.flags != null && p.flags.remove("NO_REPLENISH_ONCE")) {
            p.sendMessage("You cannot replenish your hand this turn (Fraternal Feuds).");
        } else {
            int handTarget = 3 + p.points.progressPoints;
            while (p.handSize() < handTarget) {
                p.sendMessage("PROMPT: Replenish - choose draw stack [1-4]:");
                int which = readInt(p.receiveMessage(), 1);
                stacks.drawCardfromStack(which, p);
            }
        }
    }

    private boolean checkWinEndOfTurn(Player active, Player other) {
        int score = active.currentScoreAgainst(other);
        if (score >= 7) {
            broadcast.broadcast("winner: Player " + players.indexOf(active)
                    + " wins with " + score + " VP (incl. advantage tokens)!");
            return true;
        }
        return false;
    }

    private int countFaceRegions(Player p, int face) {
        int n = 0;
        for (List<Card> row : p.principality.principality) {
            for (Card c : row) {
                if (c != null && "Region".equalsIgnoreCase(c.type) && c.diceRoll == face) {
                    n++;
                }
            }
        }
        return n;
    }

    private Player opponentOf(Player p) {
        return (p == players.get(0)) ? players.get(1) : players.get(0);
    }

    private boolean hasAdjacentBoosterForRegion(Player p, int rr, int cc) {
        Card region = p.getCard(rr, cc);
        if (region == null) {
            return false;
        }
        Card left = p.getCard(rr, cc - 1);
        Card right = p.getCard(rr, cc + 1);
        return isBoosting(left, region) || isBoosting(right, region);
    }

    private int readInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private boolean isBoosting(Card maybeBuilding, Card region) {
        if (maybeBuilding == null) {
            return false;
        }
        if (!"Building".equalsIgnoreCase(maybeBuilding.type)) {
            return false;
        }
        return buildingBoostsRegion(maybeBuilding.name, region.name);
    }

    public static boolean buildingBoostsRegion(String buildingName, String regionName) {
        if (buildingName == null || regionName == null) {
            return false;
        } else if (buildingName.equalsIgnoreCase("Iron Foundry") && regionName.equalsIgnoreCase("Mountain")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Grain Mill") && regionName.equalsIgnoreCase("Field")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Lumber Camp") && regionName.equalsIgnoreCase("Forest")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Brick Factory") && regionName.equalsIgnoreCase("Hill")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Weaver’s Shop") && regionName.equalsIgnoreCase("Pasture")) {
            return true;
        } else if (buildingName.equalsIgnoreCase("Weaver's Shop") && regionName.equalsIgnoreCase("Pasture")) {
            return true;
        }
        return false;
    }

    int nonrandeventdie = 0;
    int nonrandproductiondie = 0;

    private int getNonRandomEventDie() {
        nonrandeventdie = (nonrandeventdie + 1) % 6;
        return nonrandeventdie + 1;
    }

    protected int getNonRandomProductionDie(Player active) {
        nonrandproductiondie = (nonrandproductiondie + 1) % 6;
        int face = 1 + nonrandproductiondie;
        if (active.flags.contains("BRIGITTA")) {
            active.sendMessage("PROMPT: Brigitta active -  choose production die [1-6]:");
            try {
                int forced = Integer.parseInt(active.receiveMessage().trim());
                if (forced >= 1 && forced <= 6) {
                    face = forced;
                }
            } catch (NumberFormatException ignored) {
            }
            active.flags.remove("BRIGITTA");
        }
        return face;
    }

}
