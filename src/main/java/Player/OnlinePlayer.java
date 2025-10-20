package Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class OnlinePlayer extends Player {

 
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public OnlinePlayer() {
        super();
    }


    public void setConnection(Socket sock, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = sock;
        this.in = in;
        this.out = out;
    }

    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        socket = null;
        in = null;
        out = null;
    }


    @Override
    public void sendMessage(Object msg) {
        if (out != null) {
            try {
                out.writeObject(String.valueOf(msg));
                out.flush();
                out.reset(); 
            } catch (IOException e) {
                System.err.println("[OnlinePlayer] send error: " + e.getMessage());
            }
        } else {
            System.out.println(msg);
        }
    }

    @Override
    public String receiveMessage() {
        if (in != null && out != null) {
            try {
                Object o = in.readObject();
                return (o == null) ? null : o.toString();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[OnlinePlayer] receive error: " + e.getMessage());
                return null;
            }
        } else {
            System.out.print("> ");
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                return br.readLine();
            } catch (IOException e) {
                return null;
            }
        }
    }
}
