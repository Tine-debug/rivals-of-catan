
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
import Card.Cardstack.CardstackFacade;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class resolveOneTurnTest {

    CardstackFacade stacks = CardstackFacade.getInstance();

    static private PrintStream originalOut;
    static private PrintStream originalErr;

    private Turns turns;

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
    int[][] regionDice = {{2, 1, 6, 3, 4, 5}, {3, 4, 5, 2, 1, 6}};

    Server server = new Server();
    MockPlayer player1;
    MockPlayer player2;

    public void setupPlayers(String[] MessageP1, String[] MessageP2) {
        try {
            stacks.loadCardsForTesting("cards.json", "basic", false, true);
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
        player2.setPoints(pointsBuilder.build());
        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        turns = new Turns(players);
        server.players.add(player1);
        server.players.add(player2);
        player1.messages.add(MessageP1);
        player2.messages.add(MessageP2);
        stacks.initializePlayerPrincipality(player1, regionDice, center, 0);
        stacks.initializePlayerPrincipality(player2, regionDice, center, 1);
    }

    @BeforeEach
    public void setup() {
        try {
            stacks.loadCardsForTesting("cards.json", "basic", false, true);
        } catch (Exception e) {
        }
    }

    @Test
    public void resolveOneTurn() {
        String result = "";
        setupPlayers(new String[]{"PLAY 0", "END"}, new String[]{"PLAY 0", "END"});
        turns.resolveOneTurn(0, false);
        result += player1.printPrincipality() + "/n" + player2.printPrincipality();
        Approvals.verify(result);
    }

}
