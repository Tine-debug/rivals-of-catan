import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;


public class resolveOneTurnTest{

Cardstacks stacks = Cardstacks.getInstance();

static private PrintStream originalOut;
static private PrintStream originalErr;

@BeforeAll
    static void suppressOutput() {
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
    }

    @AfterAll
    static void restoreOutput() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }


 int center = 2;
int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };

    Server server = new MockServer();
    MockPlayer player1;
    MockPlayer player2;

    public void setupPlayers(){
        try {
                stacks.loadBasicCardsoptionalshuffle(false, "cards.json");
        } catch (Exception e) {
        }
        player1 = new MockPlayer();
        player2 = new MockPlayer();
        player2.points = new Points("4","4","4","4","4","4","4");
        server.players.add(player1);
        server.players.add(player2);
        player1.messages.add(new String []{"PLAY 0", "END", "END","END","END","END","END"});
        player2.messages.add(new String []{"PLAY 0", "END", "END","END","END","END","END"});
        server.pricipalityinitoneplayer(player1, regionDice, center, 0);
        server.pricipalityinitoneplayer(player2, regionDice, center, 1);
    }
            




    @BeforeEach
    public void setup(){
        try {
                stacks.loadBasicCardsoptionalshuffle(false, "cards.json");
        } catch (Exception e) {
        }
    }

    @Test
    public void resolveOneTurn(){
        String result = "";
            setupPlayers();
            server.resolveOneTurn(0);
            result += player1.printPrincipality() + "/n" + player2.printPrincipality();
        Approvals.verify(result);
    }






}