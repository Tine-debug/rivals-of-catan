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
        Broadcast broadcast = Broadcast.getInstance();
        broadcast.updatePlayers(players);
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
             stacks.inizializePrincipiality(p, regionDice, center, i);

        }

        addBackExtraFixedRegions();
        stacks.shuffleRegions();
    }

    public static void pricipalityinitoneplayer(Player p, int[][] regionDice, int center, int i) {
        stacks.inizializePrincipiality(p, regionDice, center, i);
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
           Turns turns = new Turns(players);
           turns.resolveOneTurn(current, true);
        }
    }
    

    

    public static boolean hasStrengthAdvantage(Player a, Player b) {
        return a.points.strengthPoints >= 3 && a.points.strengthPoints > b.points.strengthPoints;
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


    private int readInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

}
