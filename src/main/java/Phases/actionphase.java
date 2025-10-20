
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class actionphase {

    private static Cardstacks stacks = Cardstacks.getInstance();
    private static Broadcast broadcast = Broadcast.getInstance();

    private static final Map<String, String> REGION_TO_RESOURCE = Map.of(
            "Forest", "Lumber",
            "Field", "Grain",
            "Pasture", "Wool",
            "Hill", "Brick",
            "Mountain", "Ore",
            "Gold Field", "Gold");

    public static void resolvePhase(Player active, Player other) {
        boolean done = false;
        active.sendMessage("Opponent's board:");
        active.sendMessage("\t\t" + other.printPrincipality().replace("\n", "\n\t\t"));
        while (!done) {
            active.sendMessage("Your board:");
            active.sendMessage(active.printPrincipality());
            active.sendMessage("Your hand:");
            active.sendMessage(active.printHand());
            active.sendMessage("Action Phase:");
            active.sendMessage("  TRADE3 <get> <give>     — bank 3:1 ([Brick|Grain|Lumber|Wool|Ore|Gold])");
            active.sendMessage(
                    "  TRADE2 <get> <Res>      — if you have a 2:1 ship for <Res> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
            active.sendMessage(
                    "  LTS <L|R> <2from> <1to> — Large Trade Ship adjacent trade (left/right side) ([Brick|Grain|Lumber|Wool|Ore|Gold])");
            String play = "  PLAY <cardName> | <id>  — play a card from hand / play center card: ";

            ArrayList<String> buildBits = stacks.getCenterbuildingCost();

            play += String.join(", ", buildBits);
            active.sendMessage(play);
            active.sendMessage("  END                     — finish action phase");
            active.sendMessage("PROMPT: make your choice: ");
            String cmd = active.receiveMessage();
            if (cmd == null) {
                cmd = "END";
            }
            String up = cmd.trim().toUpperCase(Locale.ROOT);

            if (up.startsWith("TRADE3")) {
                String[] parts = cmd.trim().split("\\s+");
                if (parts.length >= 3) {
                    String get = parts[1];
                    String give = parts[2];
                    if (active.getResourceCount(give) >= 3) {
                        active.removeResource(give, 3);
                        active.gainResource(get);
                        broadcast.broadcast("Trade 3:1 -> +1 " + get);
                    } else {
                        active.sendMessage("Not enough " + give + " to trade 3:1.");
                    }
                } else {
                    active.sendMessage("Usage: TRADE3 <get> <give> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                }
            } else if (up.startsWith("TRADE2")) {
                String[] parts = cmd.trim().split("\\s+");
                if (parts.length >= 3) {
                    String get = parts[1];
                    String from = parts[2].toUpperCase();
                    if (active.flags.contains("2FOR1_" + from)) {
                        if (active.getResourceCount(from) >= 2) {
                            active.removeResource(from, 2);
                            active.gainResource(get);
                            broadcast.broadcast("Trade 2:1 (" + from + " ship) -> +1 " + get);
                        } else {
                            active.sendMessage("Not enough " + from + " to trade 2:1.");
                        }
                    } else {
                        active.sendMessage("You don't have a 2:1 ship for " + from + ".");
                    }
                } else {
                    active.sendMessage("Usage: TRADE2 <get> <give> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                }
            } else if (up.startsWith("LTS")) {
                String[] parts = cmd.trim().split("\\s+");
                if (parts.length >= 4) {
                    String side = parts[1].toUpperCase(); // L or R
                    String twoFrom = parts[2];
                    String oneTo = parts[3];
                    if (applyLTS(active, side, twoFrom, oneTo)) {
                        broadcast.broadcast("LTS: traded 2 " + twoFrom + " for 1 " + oneTo + " on the "
                                + (side.startsWith("L") ? "LEFT" : "RIGHT"));
                    } else {
                        active.sendMessage("LTS trade invalid here.");
                    }
                } else {
                    active.sendMessage("Usage: LTS <L|R> <2from> <1to> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                }
            } else if (up.startsWith("PLAY")) {
                String[] parts = cmd.trim().split("\\s+", 2);
                if (parts.length < 2) {
                    active.sendMessage("Usage: PLAY <cardName> | <id>");
                    continue;
                }
                String spec = parts[1].trim();

                // ---------- 1) Center cards from piles: Road / Settlement / City ----------
                if (spec.equalsIgnoreCase("Road") || spec.equalsIgnoreCase("Settlement")
                        || spec.equalsIgnoreCase("City")) {
                    int[] coordinatesplaced = stacks.placeCenterCard(active, other, spec);
                    if (coordinatesplaced[0] != -1 && coordinatesplaced[1] != -1) {
                        broadcast.broadcast("Built " + spec + " at (" + coordinatesplaced[0] + "," + coordinatesplaced[1] + ")");
                        continue;
                    }
                    continue;
                }

                Card c = active.hand.findCardInHand(active, spec);
                if (c == null) {
                    active.sendMessage("No such card in hand: " + spec);
                    continue;
                }
                if (!active.payCost(c.cost)) {
                    active.sendMessage("Can't afford cost: " + (c.cost == null ? "-" : c.cost));
                    continue;
                }

                boolean isAction = (c.type != null && c.type.toLowerCase().contains("action"));
                boolean ok;

                if (isAction) {

                    ok = c.applyEffect(active, other, -1, -1);
                    if (!ok) {
                        active.sendMessage("Action could not be resolved; refunding cost.");
                        active.refundCost(c.cost);
                        continue;
                    }

                    active.hand.hand.remove(c);
                    broadcast.broadcast("Current player played action " + c.name);
                } else {
                    // Non-action: needs placement
                    active.sendMessage("PROMPT: Enter placement coordinates as: ROW COL");
                    int row = -1, col = -1;
                    try {
                        String[] rc = active.receiveMessage().trim().split("\\s+");
                        row = Integer.parseInt(rc[0]);
                        col = Integer.parseInt(rc[1]);
                    } catch (Exception e) {
                        active.sendMessage("Invalid coordinates. Use: ROW COL (e.g., 2 3)");
                        active.refundCost(c.cost);
                        continue;
                    }

                    ok = c.applyEffect(active, other, row, col);
                    if (!ok) {
                        active.sendMessage("Illegal placement/effect; refunding cost.");
                        active.refundCost(c.cost);
                        continue;
                    }

                    active.hand.hand.remove(c);
                    broadcast.broadcast("Current player played " + c.name + " at (" + row + "," + col + ")");
                }
            } else if (up.startsWith("END")) {
                done = true;
            } else {
                active.sendMessage("Unknown command.");
            }
        }
    }

    private static boolean applyLTS(Player p, String side, String twoFrom, String oneTo) {
        // Find any LTS flag; for simplicity use the first one
        int ltsRow = -1, ltsCol = -1;
        for (String f : p.flags) {
            if (f.startsWith("LTS@")) {
                String[] rc = f.substring(4).split(",");
                try {
                    ltsRow = Integer.parseInt(rc[0]);
                    ltsCol = Integer.parseInt(rc[1]);
                } catch (Exception ignored) {
                }
                break;
            }
        }
        if (ltsRow < 0) {
            return false;
        }

        // Regions on that side are at (ltsRow, ltsCol-1) and (ltsRow, ltsCol+1)
        int takeCol = side.startsWith("L") ? ltsCol - 1 : ltsCol + 1;
        Card fromRegion = p.getCard(ltsRow, takeCol);
        Card toRegion = p.getCard(ltsRow, (side.startsWith("L") ? ltsCol + 1 : ltsCol - 1));

        if (fromRegion == null || toRegion == null) {
            return false;
        }

        // We don’t track per-resource piles, but we *do* track regionProduction; allow
        // trade if fromRegion’s
        // produced resource type matches `twoFrom` and has at least 2; grant +1 to
        // `oneTo` by increasing toRegion
        String fromType = REGION_TO_RESOURCE.getOrDefault(fromRegion.name, "");
        if (!fromType.equalsIgnoreCase(twoFrom)) {
            return false;
        }
        if (fromRegion.regionProduction < 2) {
            return false;
        }

        fromRegion.regionProduction -= 2;
        // Grant the “oneTo”: if it matches toRegion’s type, store there; else bank
        String toType = REGION_TO_RESOURCE.getOrDefault(toRegion.name, "");
        if (toType.equalsIgnoreCase(oneTo)) {
            toRegion.regionProduction = Math.min(3, toRegion.regionProduction + 1);
        } else {
            p.gainResource(oneTo);
        }
        return true;
    }

    private int readInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

}
