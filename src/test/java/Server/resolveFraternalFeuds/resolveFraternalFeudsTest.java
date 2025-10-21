
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import Card.*;
import Card.logic.*;
import Player.*;
import Turns.*;
import Turns.Cardevents.*;
import Turns.Phases.*;
import Points.*;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;


public class resolveFraternalFeudsTest{

    Cardstacks stacks = Cardstacks.getInstance();
    static private PrintStream originalOut;
    static private PrintStream originalErr;
    static CardEvent event = new fraternalfeudsEvent();

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

    Server server = new Server();
    MockPlayer player1;
    MockPlayer player2;

    public void setupPlayers(){
        try {
                stacks.sortIntoPiles(false, "cards.json");
        } catch (Exception e) {
        }
        player1 = new MockPlayer();
        player2 = new MockPlayer();
        PointsBuilder pointsBuilder = new PointsBuilder();
        pointsBuilder.victoryPoints(4);
        pointsBuilder.canonPoints(4);
        pointsBuilder.commercePoints(4);
        pointsBuilder.progressPoints(4);
        pointsBuilder.strengthPoints(4);
        pointsBuilder.skillPoints(4);
        pointsBuilder.sailPoints(4);
        player2.points = pointsBuilder.build();
        server.players.add(player1);
        server.players.add(player2);

        server.pricipalityinitoneplayer(player1, regionDice, center, 0);
        server.pricipalityinitoneplayer(player2, regionDice, center, 1);
        player2.messages.add(new String[]{"1 2"});
    }

    @Test
    public void fraternalFreudsTest(){
        setupPlayers();
        server.replenish(player1);
        server.replenish(player2);
        String result = "\n";
        result += player1.hand.printHand() + "\n";
        result += player2.hand.printHand() + "\n";
        event.resolve(player1, player2);
        result += player1.hand.printHand() + "\n";
        result += player2.hand.printHand() + "\n";

        Approvals.verify(result);

    }
    @Test
    public void fraternalFreudsemptyhandTest(){
        setupPlayers();
        String result = "\n";
        result += player1.hand.printHand() + "\n";
        result += player2.hand.printHand() + "\n";
        event.resolve(player1, player2);
        result += player1.hand.printHand() + "\n";
        result += player2.hand.printHand() + "\n";

        Approvals.verify(result);

    }

    @Test
    public void fraternalFreudsemptyNoAdvantageTest(){
        setupPlayers();
        String result = "\n";
        server.replenish(player1);
        server.replenish(player2);
        player2.points = new Points();
        result += player1.hand.printHand() + "\n";
        result += player2.hand.printHand() + "\n";
        event.resolve(player1, player2);
        result += player1.hand.printHand() + "\n";
        result += player2.hand.printHand() + "\n";

        Approvals.verify(result);

    }


           




}