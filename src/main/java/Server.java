// Server.java
// Single-process server/loop (no networking). Introductory game only.

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

public class Server {

    public final List<Player> players = new ArrayList<>();
    public ServerSocket serverSocket;
    private final Random rng = new Random();

    static private Cardstacks stacks = Cardstacks.getInstance();

    // Production helpers
    private static final Map<String, String> REGION_TO_RESOURCE = Map.of(
            "Forest", "Lumber",
            "Field", "Grain",
            "Pasture", "Wool",
            "Hill", "Brick",
            "Mountain", "Ore",
            "Gold Field", "Gold");

    // Event die faces
    private static final int EV_BRIGAND = 1;
    private static final int EV_TRADE = 2;
    private static final int EV_CELEB = 3;
    private static final int EV_PLENTY = 4;
    private static final int EV_EVENT_A = 5;
    private static final int EV_EVENT_B = 6;

    // ---------- Bootstrap ----------
    public static void main(String[] args) {
        Server s = new Server();
        try {
            if ((args.length == 0 || (args.length > 0 && args[0].equalsIgnoreCase("bot")))) {
                Card.loadBasicCards("cards.json");
                s.start(args.length == 0 ? false : true);
                s.run();
                return;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("online")) {
                s.runClient();
                return;
            } else {
                System.out.println("Usage: java Server [optional: bot|online]");
                return;
            }
        } catch (Exception e) {
            System.err.println("Failed to start: " + e.getMessage());
            return;
        }
    }

    public void start(boolean withBot) throws Exception {
        // 1) local console player
        players.add(new Player());
        // 2) bot player
        if (withBot) {
            Player bot = new Player();
            bot.isBot = true;
            players.add(bot);
        } // 3) networked players
        else {
            serverSocket = new ServerSocket(2048);
            Socket sock = serverSocket.accept();
            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
            OnlinePlayer op = new OnlinePlayer();
            op.setConnection(sock, in, out);
            players.add(op);
            System.out.println("Connected Online Player ");
            op.sendMessage("WELCOME Online Player ");
        }
        initPrincipality();

        for (int i = 0; i < players.size(); i++) {
            replenish(players.get(i));
        }
    }

    public void runClient() throws Exception {
        Socket socket = new Socket("127.0.0.1", 2048);

        // IMPORTANT: create ObjectOutputStream first, then flush, then
        // ObjectInputStream
        ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
        outToServer.flush(); // send stream header immediately
        ObjectInputStream inFromServer = new ObjectInputStream(socket.getInputStream());

        Scanner console = new Scanner(System.in);
        try {
            while (true) {
                Object obj = inFromServer.readObject();
                if (!(obj instanceof String)) {
                    // ignore unexpected payloads
                    continue;
                }
                String msg = (String) obj;

                // Always print what the server sent
                System.out.println(msg);

                // If it's a prompt, read one line from console and send it back
                if (msg.startsWith("PROMPT:")) {
                    System.out.print("> ");
                    System.out.flush();
                    String answer = console.nextLine();
                    outToServer.writeObject(answer);
                    outToServer.flush(); // push it now
                    outToServer.reset(); // avoid OOS caching of repeated String instances
                }

                // Allow server to end the session with a keyword
                if (msg.toLowerCase().contains("winner") || msg.equalsIgnoreCase("CLOSE")) {
                    break;
                }
            }
        } finally {
            try {
                console.close();
                inFromServer.close();
                outToServer.close();
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void initPrincipality() {

        int center = 2;
        int[][] regionDice = {{2, 1, 6, 3, 4, 5}, {3, 4, 5, 2, 1, 6}};

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            pricipalityinitoneplayer(p, regionDice, center, i);

        }

        addBackExtraFixedRegions();
        stacks.shuffleRegions();
    }

    public static void pricipalityinitoneplayer(Player p, int[][] regionDice, int center, int i) {
        p.placeCard(center, 1, stacks.popCardByName(Cardstacks.settlements, "Settlement"));
        p.placeCard(center, 2, stacks.popCardByName(Cardstacks.roads, "Road"));
        p.placeCard(center, 3, stacks.popCardByName(Cardstacks.settlements, "Settlement"));

        stacks.inizilizeRegion(p, regionDice, center, i);


    }

    private void addBackExtraFixedRegions() {
        // There are two of each of these cards, each with a fixed diceRoll:
        setTwoUndiced("Field", 3, 1);
        setTwoUndiced("Mountain", 4, 2);
        setTwoUndiced("Hill", 5, 1);
        setTwoUndiced("Forest", 6, 4);
        setTwoUndiced("Pasture", 6, 5);
        setTwoUndiced("Gold Field", 3, 2);

        // After assigning dice to remaining cards, shuffle the deck
        stacks.shuffleRegions();
    }

    private void setTwoUndiced(String name, int d1, int d2) {
        Card c1 = stacks.findUndicedRegionByName(name);
        if (c1 != null) {
            c1.diceRoll = d1;
        }
        Card c2 = stacks.findUndicedRegionByName(name);
        if (c2 != null) {
            c2.diceRoll = d2;
        }
    }

    // Returns a card with diceRoll == 0, matching name, but DOES NOT remove it.


    public void run() {
        int current = Math.random() < 0.5 ? 0 : 1;
        for (int i = 0; i < players.size(); i++) {
            players.get(i).sendMessage("Opponent's starting board:");
            players.get(i).sendMessage(
                    "\t\t" + players.get((i + 1) % players.size()).printPrincipality().replace("\n", "\n\t\t"));
            players.get(i).sendMessage("Your starting board:");
            players.get(i).sendMessage(players.get(i).printPrincipality());
            players.get(i).sendMessage("Your starting hand:");
            players.get(i).sendMessage(players.get(i).printHand());
        }
        while (true) {
            if (current == 3) {
                break;
            }
            current = resolveOneTurn(current);
        }
    }

    public int resolveOneTurn(int current) {
        Player active = players.get(current);
        Player other = players.get((current + 1) % players.size());

        int eventFace = rollEventDie(active);
        int prodFace = rollProductionDie(active);

        if (eventFace == EV_BRIGAND) {
            resolveEvent(eventFace, active, other);
            applyProduction(prodFace);
        } else {
            applyProduction(prodFace);
            resolveEvent(eventFace, active, other);
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

        actionPhase(active, other);

        replenish(active);

        exchangePhase(active);
        if (checkWinEndOfTurn(active, other)) {
            return 3;
        }

        current = (current + 1) % players.size();

        return current;

    }

    private boolean checkWinEndOfTurn(Player active, Player other) {
        int score = active.currentScoreAgainst(other);
        if (score >= 7) {
            broadcast("winner: Player " + players.indexOf(active)
                    + " wins with " + score + " VP (incl. advantage tokens)!");
            return true;
        }
        return false;
    }

    protected int rollEventDie(Player active) {
        int face = 1 + rng.nextInt(6);
        broadcast("[EventDie] -> " + face);
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
            } catch (Exception ignored) {
            }
            active.flags.remove("BRIGITTA");
        }
        broadcast("[ProductionDie] -> " + face);
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

    private boolean hasAdjacentBoosterForRegion(Player p, int rr, int cc) {
        Card region = p.getCard(rr, cc);
        if (region == null) {
            return false;
        }
        Card left = p.getCard(rr, cc - 1);
        Card right = p.getCard(rr, cc + 1);
        return isBoosting(left, region) || isBoosting(right, region);
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
        }else if (buildingName.equalsIgnoreCase("Iron Foundry") && regionName.equalsIgnoreCase("Mountain")) {
            return true; 
        }else if (buildingName.equalsIgnoreCase("Grain Mill") && regionName.equalsIgnoreCase("Field")) {
            return true; 
        }else if (buildingName.equalsIgnoreCase("Lumber Camp") && regionName.equalsIgnoreCase("Forest")) {
            return true; 
        }else if (buildingName.equalsIgnoreCase("Brick Factory") && regionName.equalsIgnoreCase("Hill")) {
            return true; 
        }else if (buildingName.equalsIgnoreCase("Weaver’s Shop") && regionName.equalsIgnoreCase("Pasture")) {
            return true; 
        }else if (buildingName.equalsIgnoreCase("Weaver's Shop") && regionName.equalsIgnoreCase("Pasture")) {
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

    private void resolveEvent(int face, Player active, Player other) {
        switch (face) {
            case EV_BRIGAND:
                broadcast("[Event] Brigand Attack");
                for (Player p : players) {
                    int total = countGoldAndWool(p, true);
                    if (total > 7) {
                        zeroGoldAndWool(p, true);
                        p.sendMessage("Brigands! You lose all Gold & Wool in affected regions.");
                    }
                }
                break;

            case EV_TRADE:
                broadcast("[Event] Trade");
                for (Player p : players) {
                    if (p.points.commercePoints >= 3) {
                        p.sendMessage(
                                "PROMPT: Trade Advantage - gain 1 resource of your choice [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                        p.gainResource(p.receiveMessage());
                    }
                }
                break;

            case EV_CELEB:
                broadcast("[Event] Celebration");
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
                broadcast("[Event] Plentiful Harvest: each player gains 1 of choice.");
                for (Player p : players) {
                    p.sendMessage("PROMPT: Plentiful Harvest - choose a resource [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    p.gainResource(p.receiveMessage());
                    if (p.flags.contains("TOLLB")) {
                        int add = grantGoldIfSpace(p, 2);
                        if (add > 0) {
                            p.sendMessage("Toll Bridge: +" + add + " Gold");
                        }
                    }
                }
                break;

            case EV_EVENT_A:
            case EV_EVENT_B: {
                broadcast("[Event] Draw Event Card");
                if (Cardstacks.events.isEmpty()) {
                    broadcast("Event deck empty.");
                    break;
                }
                Card top = Cardstacks.events.remove(0);
                broadcast("EVENT: " + (top.cardText != null ? top.cardText : top.name));

                String nm = (top.name == null ? "" : top.name).toLowerCase();

                if (nm.equalsIgnoreCase("feud")) {
                    resolveFeud(active, other);
                } else if (nm.equalsIgnoreCase("fraternal feuds")) {
                    resolveFraternalFeuds(active, other);
                } else if (nm.equalsIgnoreCase("invention")) {
                    resolveInvention();
                } else if (nm.equalsIgnoreCase("trade ships race")) {
                    resolveTradeShipsRace();
                } else if (nm.equalsIgnoreCase("traveling merchant")) {
                    resolveTravelingMerchant();
                } else if (nm.equalsIgnoreCase("year of plenty")) {
                    resolveYearOfPlenty();
                } else if (nm.equalsIgnoreCase("yule")) {
                    java.util.Collections.shuffle(Cardstacks.events);
                    resolveEvent(EV_EVENT_A, active, other);
                }
                break;
            }
            default:
                broadcast("[Event] Unknown face " + face);
        }
    }

    private void resolveFeud(Player active, Player other) {
        Player adv = hasStrengthAdvantage(players.get(0), players.get(1)) ? players.get(0)
                : hasStrengthAdvantage(players.get(1), players.get(0)) ? players.get(1)
                : null;

        if (adv == null) {
            broadcast("Feud: no strength advantage; nothing happens.");
            return;
        }

        Player opp = (adv == players.get(0)) ? players.get(1) : players.get(0);

        java.util.List<int[]> buildings = new java.util.ArrayList<>();
        for (int r = 0; r < opp.principality.principality.size(); r++) {
            var row = opp.principality.principality.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x != null && x.type != null && x.type.equalsIgnoreCase("Building")) {
                    buildings.add(new int[]{r, c});
                }
            }
        }
        if (buildings.isEmpty()) {
            broadcast("Feud: opponent has no buildings.");
            return;
        }
        adv.sendMessage(
                "PROMPT: Feud - select up to 3 opponent building coordinates as 'r c;r c;r c'. Opponent board:\n"
                + opp.printPrincipality());
        String line = adv.receiveMessage();
        java.util.List<int[]> picked = new java.util.ArrayList<>();
        try {
            for (String pair : line.split(";")) {
                String s = pair.trim();
                if (s.isEmpty()) {
                    continue;
                }
                String[] rc = s.split("\\s+");
                int r = Integer.parseInt(rc[0]);
                int c = Integer.parseInt(rc[1]);
                // validate it's a building
                Card x = getSafe(opp, r, c);
                if (x != null && x.type != null && x.type.equalsIgnoreCase("Building")) {
                    picked.add(new int[]{r, c});
                    if (picked.size() == 3) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        int k = 0;
        while (picked.size() < 3 && k < buildings.size()) {
            int[] bc = buildings.get(k++);
            boolean dup = false;
            for (int[] pc : picked) {
                if (pc[0] == bc[0] && pc[1] == bc[1]) {
                    dup = true;
                    break;
                }
            }
            if (!dup) {
                picked.add(bc);
            }
        }
        if (picked.isEmpty()) {
            broadcast("Feud: no valid targets selected/found.");
            return;
        }

        StringBuilder opts = new StringBuilder("PROMPT: Feud - choose which to remove (index 0..")
                .append(picked.size() - 1)
                .append("):\n");
        for (int i = 0; i < picked.size(); i++) {
            int r = picked.get(i)[0], c = picked.get(i)[1];
            Card x = getSafe(opp, r, c);
            opts.append("  [").append(i).append("] (").append(r).append(",").append(c).append(") ").append(x)
                    .append("\n");
        }
        opp.sendMessage(opts.toString());
        int choice = 0;
        try {
            choice = Integer.parseInt(opp.receiveMessage().trim());
        } catch (Exception ignored) {
        }
        if (choice < 0 || choice >= picked.size()) {
            choice = 0;
        }
        int rr = picked.get(choice)[0], cc = picked.get(choice)[1];
        Card removed = opp.principality.principality.get(rr).set(cc, null);
        broadcast("Feud: removed " + (removed == null ? "unknown" : removed.name) + " from opponent at (" + rr + ","
                + cc + ").");
        returnBuildingToBottom(removed);
    }

    public void resolveFraternalFeuds(Player active, Player other) {
        Player adv = hasStrengthAdvantage(players.get(0), players.get(1)) ? players.get(0)
                : hasStrengthAdvantage(players.get(1), players.get(0)) ? players.get(1)
                : null;

        if (adv == null) {
            broadcast("Fraternal Feuds: no strength advantage; nothing happens.");
            return;
        }
        Player opp = (adv == players.get(0)) ? players.get(1) : players.get(0);

        if (opp.hand.hand.isEmpty()) {
            broadcast("Fraternal Feuds: opponent hand empty.");
            return;
        }

        adv.sendMessage("PROMPT: Opponent hand:\n" + opp.printHand() + "Choose up to two indices (e.g., '2 5'):");
        String sel = adv.receiveMessage();
        java.util.Set<Integer> idxs = new java.util.HashSet<>();
        try {
            for (String tok : sel.trim().split("\\s+")) {
                int i = Integer.parseInt(tok);
                if (i >= 0 && i < opp.hand.hand.size()) {
                    idxs.add(i);
                }
                if (idxs.size() == 2) {
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        // If insufficient/invalid, take first one or two
        if (idxs.isEmpty()) {
            idxs.add(0);
            if (opp.hand.handSize() > 1) {
                idxs.add(1);
            }
        }

        // Remove in descending order so indices stay valid
        java.util.List<Integer> order = new java.util.ArrayList<>(idxs);
        java.util.Collections.sort(order, java.util.Collections.reverseOrder());
        for (int i : order) {
            Card rem = opp.hand.hand.remove(i);
            returnBuildingToBottom(rem);
            broadcast("Fraternal Feuds: returned '" + rem.name + "' to bottom of a draw stack.");
        }

        markSkipReplenishOnce(opp);
        broadcast("Fraternal Feuds: opponent cannot replenish hand at the end of the next turn.");
    }

    private void resolveTradeShipsRace() {
        int c0 = countTradeShips(players.get(0));
        int c1 = countTradeShips(players.get(1));

        if (c0 == 0 && c1 == 0) {
            broadcast("Trade Ships Race: no one owns trade ships.");
            return;
        }
        if (c0 > c1) {
            Player p = players.get(0);
            p.sendMessage(
                    "PROMPT: Trade Ships Race - you have the most trade ships. Choose 1 resource [Brick|Grain|Lumber|Wool|Ore|Gold]:");
            p.gainResource(p.receiveMessage());
        } else if (c1 > c0) {
            Player p = players.get(1);
            p.sendMessage(
                    "PROMPT: Trade Ships Race - you have the most trade ships. Choose 1 resource [Brick|Grain|Lumber|Wool|Ore|Gold]:");
            p.gainResource(p.receiveMessage());
        } else { // tie
            if (c0 >= 1 && c1 >= 1) {
                for (Player p : players) {
                    p.sendMessage(
                            "PROMPT: Trade Ships Race (tie) - choose 1 resource [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    p.gainResource(p.receiveMessage());
                }
            } else {
                broadcast("Trade Ships Race: tie without both having ≥1 ship; no one receives a resource.");
            }
        }
    }

    private void resolveTravelingMerchant() {
        for (Player p : players) {
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

    private void resolveYearOfPlenty() {
        for (Player p : players) {
            int added = 0;
            for (int r = 0; r < p.principality.principality.size(); r++) {
                var row = p.principality.principality.get(r);
                for (int c = 0; c < row.size(); c++) {
                    Card reg = row.get(c);
                    if (reg == null || !"Region".equalsIgnoreCase(reg.type)) {
                        continue;
                    }

                    int adj = countAdjStorehouseAbbey(p, r, c);
                    while (adj-- > 0) {
                        if (reg.regionProduction < 3) {
                            reg.regionProduction++;
                            added++;
                        }
                    }
                }
            }
            p.sendMessage("Year of Plenty: resources were added to your regions where adjacent to Storehouse/Abbey.");
        }
    }

    private void resolveInvention() {
        for (Player p : players) {
            int times = Math.min(2, Math.max(0, p.points.progressPoints));
            if (times == 0) {
                p.sendMessage("Invention: you have no progress point buildings (max 2).");
                continue;
            }
            for (int i = 0; i < times; i++) {
                p.sendMessage("PROMPT: Invention - gain resource #" + (i + 1) + " of your choice:");
                String res = p.receiveMessage();
                p.gainResource(res);
            }
        }
    }

    private void returnBuildingToBottom(Card bld) {
        if (bld == null) {
            return;
        }
        // Super quick: push to bottom of drawStack1
        Cardstacks.drawStack1.add(bld);
    }

    private void markSkipReplenishOnce(Player p) {
        if (p.flags == null) {
            p.flags = new java.util.HashSet<>();
        }
        p.flags.add("NO_REPLENISH_ONCE");
    }

    private boolean hasStrengthAdvantage(Player a, Player b) {
        // Simple: >=3 Strength and strictly more than opponent
        return a.points.strengthPoints >= 3 && a.points.strengthPoints > b.points.strengthPoints;
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

    // Helpers for Brigand / Toll Bridge
    private int countGoldAndWool(Player p, boolean excludeStorehouseAdj) {
        int total = 0;
        Set<String> excluded = excludeStorehouseAdj ? storehouseExcludedKeys(p) : Set.of();
        for (int r = 0; r < p.principality.principality.size(); r++) {
            java.util.List<Card> row = p.principality.principality.get(r);
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
                if ("Gold Field".equalsIgnoreCase(card.name) || "Pasture".equalsIgnoreCase(card.name)) {
                    total += Math.max(0, Math.min(3, card.regionProduction));
                }
            }
        }

        return total;
    }

    private void zeroGoldAndWool(Player p, boolean excludeStorehouseAdj) {
        Set<String> excluded = excludeStorehouseAdj ? storehouseExcludedKeys(p) : Set.of();
        forEachRegion(p, (r, c, card) -> {
            if (card == null) {
                return;
            }
            String key = r + ":" + c;
            if (excluded.contains(key)) {
                return;
            }
            if ("Gold Field".equalsIgnoreCase(card.name) || "Pasture".equalsIgnoreCase(card.name)) {
                card.regionProduction = 0;
            }
        });
    }

    private int grantGoldIfSpace(Player p, int want) {
        int given = 0;
        for (int r = 0; r < p.principality.principality.size(); r++) {
            java.util.List<Card> row = p.principality.principality.get(r);
            if (row == null) {
                continue;
            }
            for (int c = 0; c < row.size(); c++) {
                if (given >= want) {
                    break; // stop if already satisfied

                                }Card card = row.get(c);
                if (card != null && "Gold Field".equalsIgnoreCase(card.name)) {
                    int can = Math.max(0, 3 - card.regionProduction);
                    int add = Math.min(can, want - given);
                    if (add > 0) {
                        card.regionProduction += add;
                        given += add;
                    }
                }
            }
        }
        return given;
    }

    // Storehouse excludes regions immediately left/right of each Storehouse (same
    // side of center)
    private Set<String> storehouseExcludedKeys(Player p) {
        Set<String> out = new HashSet<>();
        for (int r = 0; r < p.principality.principality.size(); r++) {
            List<Card> row = p.principality.principality.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x != null && x.name != null && x.name.equalsIgnoreCase("Storehouse")) {
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

    private static boolean nmAt(Card c, String a, String b) {
        if (c == null || c.name == null) {
            return false;
        }
        return c.name.equalsIgnoreCase(a) || c.name.equalsIgnoreCase(b);
    }

    private interface RegionVisitor {

        void visit(int r, int c, Card card);
    }

    private void forEachRegion(Player p, RegionVisitor v) {
        for (int r = 0; r < p.principality.principality.size(); r++) {
            List<Card> row = p.principality.principality.get(r);
            for (int c = 0; c < row.size(); c++) {
                Card card = row.get(c);
                if (card != null && "Region".equalsIgnoreCase(card.type)) {
                    v.visit(r, c, card);
                }
            }
        }
    }

    private Player opponentOf(Player p) {
        return (p == players.get(0)) ? players.get(1) : players.get(0);
    }

    // ---------- Actions ----------
    private void actionPhase(Player active, Player other) {
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

            ArrayList<String> buildBits = new ArrayList<>();
            if (!Cardstacks.roads.isEmpty()) {
                String cost = Cardstacks.roads.get(0).cost == null ? "-" : Cardstacks.roads.get(0).cost;
                buildBits.add("ROAD(" + cost + ")");
            }
            if (!Cardstacks.settlements.isEmpty()) {
                String cost = Cardstacks.settlements.get(0).cost == null ? "-" : Cardstacks.settlements.get(0).cost;
                buildBits.add("SETTLEMENT(" + cost + ")");
            }
            if (!Cardstacks.cities.isEmpty()) {
                String cost = Cardstacks.cities.get(0).cost == null ? "-" : Cardstacks.cities.get(0).cost;
                buildBits.add("CITY(" + cost + ")");
            }
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
                        broadcast("Trade 3:1 -> +1 " + get);
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
                            broadcast("Trade 2:1 (" + from + " ship) -> +1 " + get);
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
                        broadcast("LTS: traded 2 " + twoFrom + " for 1 " + oneTo + " on the "
                                + (side.startsWith("L") ? "LEFT" : "RIGHT")); 
                    }else {
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
                    Vector<Card> pile = null;
                    if (spec.equalsIgnoreCase("Road")) {
                        pile = Cardstacks.roads; 
                    }else if (spec.equalsIgnoreCase("Settlement")) {
                        pile = Cardstacks.settlements; 
                    }else if (spec.equalsIgnoreCase("City")) {
                        pile = Cardstacks.cities;
                    }

                    if (pile == null || pile.isEmpty()) {
                        active.sendMessage("No " + spec + " cards left in the pile.");
                        continue;
                    }

                    // Peek (do not remove yet)
                    Card proto = pile.firstElement();

                    // Check & pay cost first (do NOT mutate piles yet)
                    if (!payCost(active, proto.cost)) {
                        active.sendMessage("Can't afford cost: " + (proto.cost == null ? "-" : proto.cost));
                        continue;
                    }

                    // Ask coordinates and attempt placement
                    active.sendMessage("PROMPT: Enter placement coordinates as: ROW COL");
                    int row = -1, col = -1;
                    try {
                        String[] rc = active.receiveMessage().trim().split("\\s+");
                        row = Integer.parseInt(rc[0]);
                        col = Integer.parseInt(rc[1]);
                    } catch (Exception e) {
                        active.sendMessage("Invalid coordinates. Use: ROW COL (e.g., 2 3)");
                        refundCost(active, proto.cost);
                        continue;
                    }

                    boolean ok = proto.applyEffect(active, other, row, col);
                    if (!ok) {
                        active.sendMessage("Illegal placement/effect; refunding cost.");
                        refundCost(active, proto.cost);
                        continue;
                    }

                    // Success → remove from pile now
                    pile.remove(0);
                    broadcast("Built " + spec + " at (" + row + "," + col + ")");
                    continue;
                }

                Card c = active.hand.findCardInHand(active, spec);
                if (c == null) {
                    active.sendMessage("No such card in hand: " + spec);
                    continue;
                }
                if (!payCost(active, c.cost)) {
                    active.sendMessage("Can't afford cost: " + (c.cost == null ? "-" : c.cost));
                    continue;
                }

                boolean isAction = (c.type != null && c.type.toLowerCase().contains("action"));
                boolean ok;

                if (isAction) {

                    ok = c.applyEffect(active, other, -1, -1);
                    if (!ok) {
                        active.sendMessage("Action could not be resolved; refunding cost.");
                        refundCost(active, c.cost);
                        continue;
                    }

                    active.hand.hand.remove(c);
                    broadcast("Current player played action " + c.name);
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
                        refundCost(active, c.cost);
                        continue;
                    }

                    ok = c.applyEffect(active, other, row, col);
                    if (!ok) {
                        active.sendMessage("Illegal placement/effect; refunding cost.");
                        refundCost(active, c.cost);
                        continue;
                    }

                    active.hand.hand.remove(c);
                    broadcast("Current player played " + c.name + " at (" + row + "," + col + ")");
                }
            } else if (up.startsWith("END")) {
                done = true;
            } else {
                active.sendMessage("Unknown command.");
            }
        }
    }

    private boolean payCost(Player p, String cost) {
        if (cost == null || cost.isBlank()) {
            return true;
        }
        // Cost like "1 Brick, 1 Grain, 1 Wool, 1 Lumber" etc.
        Map<String, Integer> need = parseCost(cost);
        for (var e : need.entrySet()) {
            if (p.getResourceCount(e.getKey()) < e.getValue()) {
                return false;
            }
        }
        for (var e : need.entrySet()) {
            p.removeResource(e.getKey(), e.getValue());
        }
        return true;
    }

    private void refundCost(Player p, String cost) {
        if (cost == null || cost.isBlank()) {
            return;
        }
        Map<String, Integer> need = parseCost(cost);
        for (var e : need.entrySet()) {
            p.setResourceCount(e.getKey(), p.getResourceCount(e.getKey()) + e.getValue());
        }
    }

    // Maps single-letter cost codes to canonical resource names used everywhere
    // else.
    // B=Brick, G=Grain, L=Lumber, W=Wool, O=Ore, A=Gold
    private String letterToResource(char ch) {
        switch (Character.toUpperCase(ch)) {
            case 'B':
                return "Brick";
            case 'G':
                return "Grain";
            case 'L':
                return "Lumber";
            case 'W':
                return "Wool";
            case 'O':
                return "Ore";
            case 'A':
                return "Gold";
            default:
                return null; // unknown / ignore
        }
    }

    private Map<String, Integer> parseCost(String cost) {
        Map<String, Integer> m = new HashMap<>();
        if (cost == null) {
            return m;
        }

        // Accept strings like "LW", "AA", with optional spaces or separators ("L,W", "A
        // A")
        for (int i = 0; i < cost.length(); i++) {
            char ch = cost.charAt(i);
            if (Character.isWhitespace(ch) || ch == ',' || ch == ';' || ch == '+') {
                continue;
            }
            String res = letterToResource(ch);
            if (res != null) {
                m.put(res, m.getOrDefault(res, 0) + 1);
            }
            // else: silently ignore unknown chars
        }
        return m;
    }

    // Large Trade Ship trade: side L/R relative to a placed LTS@row,col
    private boolean applyLTS(Player p, String side, String twoFrom, String oneTo) {
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
        Card fromRegion = getSafe(p, ltsRow, takeCol);
        Card toRegion = getSafe(p, ltsRow, (side.startsWith("L") ? ltsCol + 1 : ltsCol - 1));

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

    private Card getSafe(Player p, int r, int c) {
        return p.getCard(r, c);
    }

    // Very simple adjacency for YOP: count Storehouse/Abbey directly above/below
    // same column
    private int countAdjStorehouseAbbey(Player p, int rr, int cc) {
        int cnt = 0;
        Card up = getSafe(p, rr - 1, cc);
        Card down = getSafe(p, rr + 1, cc);
        if (up != null && up.name != null) {
            String n = up.name.toLowerCase();
            if (n.equals("storehouse") || n.equals("abbey")) {
                cnt++;
            }
        }
        if (down != null && down.name != null) {
            String n = down.name.toLowerCase();
            if (n.equals("storehouse") || n.equals("abbey")) {
                cnt++;
            }
        }
        return cnt;
    }

    // ---------- Replenish ----------
    public void replenish(Player p) {
        if (p.flags != null && p.flags.remove("NO_REPLENISH_ONCE")) {
            p.sendMessage("You cannot replenish your hand this turn (Fraternal Feuds).");
            return;
        } else {
            int handTarget = 3 + p.points.progressPoints;
            while (p.handSize() < handTarget) {
                p.sendMessage("PROMPT: Replenish - choose draw stack [1-4]:");
                int which = readInt(p.receiveMessage(), 1);
                Vector<Card> stack = stackBy(which);
                if (stack.isEmpty()) {
                    // advance circularly until any non-empty
                    int tries = 0;
                    do {
                        which = 1 + (which % 4);
                        stack = stackBy(which);
                        tries++;
                    } while (stack.isEmpty() && tries <= 4);
                    if (stack.isEmpty()) {
                        p.sendMessage("All stacks empty.");
                        break;
                    }
                }
                p.addToHand(stack.remove(0));
            }
        }
    }

    private Vector<Card> stackBy(int n) {
        switch (n) {
            case 1:
                return stacks.drawStack1;
            case 2:
                return stacks.drawStack2;
            case 3:
                return stacks.drawStack3;
            case 4:
                return stacks.drawStack4;
        }
        return stacks.drawStack1;
    }

    private int readInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    // ---------- Exchange (with Parish Hall discount) ----------
    private void exchangePhase(Player p) {
        int limit = 3 + p.points.progressPoints;
        if (p.handSize() < limit) {
            broadcast("Exchange: hand below limit; skipping.");
            return;
        }

        p.sendMessage("PROMPT: Exchange a card? (Y/N)");
        String ans = p.receiveMessage();
        if (ans == null || !ans.trim().toUpperCase().startsWith("Y")) {
            return;
        }

        p.sendMessage("PROMPT: Enter card name to put under a stack:");
        String nm = p.receiveMessage();
        Card chosen = p.removeFromHandByName(nm);
        if (chosen == null) {
            p.sendMessage("Not in hand.");
            return;
        }

        p.sendMessage("PROMPT: Choose stack [1-4] to put it under:");
        int st = readInt(p.receiveMessage(), 1);
        Vector<Card> stack = stackBy(st);
        stack.add(chosen);

        boolean hasParish = p.flags.contains("PARISH");
        int searchCost = hasParish ? 1 : 2;

        p.sendMessage("PROMPT: Choose Random draw (R) or Search (S, costs " + searchCost + " any)?");
        String mode = p.receiveMessage();
        if (mode != null && mode.trim().toUpperCase().startsWith("S")) {
            // Pay 1 (with Parish) or 2 (normal) resources of the player's choice
            if (p.totalAllResources() < searchCost) {
                p.sendMessage("Not enough resources to search.");
                return;
            }
            for (int i = 0; i < searchCost; i++) {
                p.sendMessage("PROMPT: Discard resource #" + (i + 1) + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                p.removeResource(p.receiveMessage(), 1);
            }

            if (stack.isEmpty()) {
                p.sendMessage("That stack is empty.");
                return;
            }
            p.sendMessage("Stack contains (top..bottom):");
            for (Card c : stack) {
                p.sendMessage(" - " + c.name);
            }
            p.sendMessage("PROMPT: Type exact name to take:");
            String take = p.receiveMessage();
            for (int i = 0; i < stack.size(); i++) {
                if (stack.get(i).name.equalsIgnoreCase(take)) {
                    p.addToHand(stack.remove(i));
                    return;
                }
            }
            p.sendMessage("Not found; no card taken.");
        } else {
            // Random draw (top of chosen stack)
            if (stack.isEmpty()) {
                p.sendMessage("That stack is empty.");
                return;
            }
            p.addToHand(stack.remove(0));
        }
    }

    // ---------- Misc ----------
    private void broadcast(String s) {
        // send to each player
        for (Player p : players) {
            if (p != null) {
                p.sendMessage(s);
            }
        }
    }
}
