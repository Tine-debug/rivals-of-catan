package Player;

import java.util.ArrayList;
import java.util.List;

public class Broadcast {

    private static Broadcast instance;
    private List<Player> players = new ArrayList<>();

    private Broadcast() {

    }

    public static Broadcast getInstance() {
        if (instance == null) {
            instance = new Broadcast();
        }
        return instance;
    }

    public void updatePlayers(List<Player> newplayers) {
        players = newplayers;
    }

    public void broadcast(String s) {
        for (Player p : players) {
            if (p != null) {
                p.sendMessage(s);
            }
        }
    }

}
