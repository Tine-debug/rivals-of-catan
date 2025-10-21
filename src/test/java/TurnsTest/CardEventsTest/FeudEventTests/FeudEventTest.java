package TurnsTest.CardEventsTest.FeudEventTests;

import Utils.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import java.io.IOException;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import Card.*;
import Card.Cardstack.CardstackFacade;
import Card.logic.*;
import Player.*;
import Turns.*;
import Turns.Cardevents.*;
import Turns.Phases.*;
import Points.*;

public class FeudEventTest {

    static Card genericBuilding;

    MockPlayer player1;
    MockPlayer player2;
    static CardEvent feudEvent = new feudEvent();
    CardstackFacade stacks = CardstackFacade.getInstance();

    @BeforeAll
    static void setup() {
        Cardbuilder builder = new Cardbuilder();
        builder.name("Generic Building");
        builder.type("Building");

        PointsBuilder pointsBuilder = new PointsBuilder();
        builder.points(pointsBuilder.build());

        genericBuilding = builder.build();

        TestUtils.suppressOutput();
    }

    @AfterAll
    static void restore() {
        TestUtils.restoreOutput();
    }

    @Test
    public void NoStrenghtAdventage() {
        String result = "";

        player1 = TestUtils.inizializePlayer(0);
        player2 = TestUtils.inizializePlayer(1);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        feudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);
    }

    @Test
    public void NoBuildings() {
        String result = "";

        player1 = TestUtils.inizializePlayer(0);
        player2 = TestUtils.inizializePlayer(1);
        PointsBuilder pointsBuilder = new PointsBuilder();
        pointsBuilder.strengthPoints(5);
        player1.setPoints(pointsBuilder.build());

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        feudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);

    }

    @Test
    public void OneBuilding() {
        String result = "";

        player1 = TestUtils.inizializePlayer(0);
        player1.placeCard(1, 1, genericBuilding);

        player2 = TestUtils.inizializePlayer(1);

        player2.placeCard(1, 1, genericBuilding);
        PointsBuilder pointsBuilder = new PointsBuilder();
        pointsBuilder.strengthPoints(5);
        player1.setPoints(pointsBuilder.build());
        player1.messages.add("1 1");

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        feudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);

    }

    @Test
    public void TwoBuildings() {
        String result = "";

        player1 = TestUtils.inizializePlayer(0);

        player2 = TestUtils.inizializePlayer(1);

        player2.placeCard(1, 1, genericBuilding);
        player2.placeCard(3, 1, genericBuilding);
        PointsBuilder pointsBuilder = new PointsBuilder();
        pointsBuilder.strengthPoints(5);
        player1.setPoints(pointsBuilder.build());
        player1.messages.add("1 1; 3 1");
        player2.messages.add("1");
        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        feudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);

    }

    @Test
    public void ThreeBuildings() {
        String result = "";

        player1 = TestUtils.inizializePlayer(0);

        player2 = TestUtils.inizializePlayer(1);

        player2.placeCard(1, 1, genericBuilding);
        player2.placeCard(3, 1, genericBuilding);
        player2.placeCard(3, 3, genericBuilding);
        PointsBuilder pointsBuilder = new PointsBuilder();
        pointsBuilder.strengthPoints(5);
        player1.setPoints(pointsBuilder.build());
        player1.messages.add("1 1; 3 1; 3 3");
        player2.messages.add("1");
        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        feudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);

    }

        @Test
    public void NoAnswer() {
        String result = "";

        player1 = TestUtils.inizializePlayer(0);

        player2 = TestUtils.inizializePlayer(1);

        player2.placeCard(1, 1, genericBuilding);
        player2.placeCard(3, 1, genericBuilding);
        player2.placeCard(3, 3, genericBuilding);
        PointsBuilder pointsBuilder = new PointsBuilder();
        pointsBuilder.strengthPoints(5);
        player1.setPoints(pointsBuilder.build());
        player1.messages.add("");
        player2.messages.add("1");
        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        feudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";


        player1 = TestUtils.inizializePlayer(0);

        player2 = TestUtils.inizializePlayer(1);

        player2.placeCard(1, 1, genericBuilding);
        player2.placeCard(3, 1, genericBuilding);
        player2.placeCard(3, 3, genericBuilding);
        pointsBuilder = new PointsBuilder();
        pointsBuilder.strengthPoints(5);
        player1.setPoints(pointsBuilder.build());
        player1.messages.add("");
        player2.messages.add("1");
        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        feudEvent.resolve(player2, player1);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);

    }

        @Test
    public void WrongChoice() {
        String result = "";

        player1 = TestUtils.inizializePlayer(0);

        player2 = TestUtils.inizializePlayer(1);

        player2.placeCard(1, 1, genericBuilding);
        player2.placeCard(3, 1, genericBuilding);
        player2.placeCard(3, 3, genericBuilding);
        PointsBuilder pointsBuilder = new PointsBuilder();
        pointsBuilder.strengthPoints(5);
        player1.setPoints(pointsBuilder.build());
        player1.messages.add("1 1; 3 1; 3 3");
        player2.messages.add("-1");
        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        feudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);

    }


}
