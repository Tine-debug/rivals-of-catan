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

    public void updatePlayers(List<Player> newplayers){
        players = newplayers;
    }

    public void broadcast(String s){
        for (Player p : players) {
            if (p != null) {
                p.sendMessage(s);
            }
        }
    }


  

}
