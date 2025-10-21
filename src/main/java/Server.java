
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Card.Cardstacks;
import Player.Player;
import Player.OnlinePlayer;
import Player.Broadcast;
import Card.Card;
import Turns.Turns;


public class Server {

    public final List<Player> players = new ArrayList<>();
    public ServerSocket serverSocket;
    static final private Cardstacks stacks = Cardstacks.getInstance();



    public static void main(String[] args) {
        Server s = new Server();
        try {
            if ((args.length == 0 || (args.length > 0 && args[0].equalsIgnoreCase("bot")))) {
                stacks.loadBasicCards("cards.json");
                s.start((args.length != 0));
                s.run();
            } else if (args.length > 0 && args[0].equalsIgnoreCase("online")) {
                s.runClient();
            } else {
                System.out.println("Usage: java Server [optional: bot|online]");
            }
        } catch (Exception e) {
            System.err.println("Failed to start: " + e.getMessage());
        }
    }

    public void start(boolean withBot) throws Exception {
        // 1) local console player
        players.add(new Player());
        // 2) bot player
        if (withBot) {
            Player bot = new Player();
            bot.setIsBot(true);
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
            } catch (IOException ignored) {
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
            c1.setDiceRoll(d1);
        }
        Card c2 = stacks.findUndicedRegionByName(name);
        if (c2 != null) {
            c2.setDiceRoll(d2);
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
    


    public void replenish(Player p) {
        if (p.getFlags() != null && p.getFlags().remove("NO_REPLENISH_ONCE")) {
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
        } catch (NumberFormatException e) {
            return def;
        }
    }

}
