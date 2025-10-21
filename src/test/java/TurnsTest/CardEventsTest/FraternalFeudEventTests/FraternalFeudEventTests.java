package TurnsTest.CardEventsTest.FraternalFeudEventTests;

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

public class FraternalFeudEventTests {

    static Card genericBuilding;

    MockPlayer player1;
    MockPlayer player2;
    static CardEvent fraternalfeudEvent = new fraternalfeudsEvent();
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

        player1.getHand().addToHand(genericBuilding);
        player1.getHand().addToHand(genericBuilding);

        player2.getHand().addToHand(genericBuilding);
        player2.getHand().addToHand(genericBuilding);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        fraternalfeudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);
    }

    @Test
    public void twoCardsBulshitAnswer() {
        String result = "";

        player1 = TestUtils.inizializePlayer(0);
        player2 = TestUtils.inizializePlayer(1);

        player1.getHand().addToHand(genericBuilding);
        player1.getHand().addToHand(genericBuilding);

        PointsBuilder builder = new PointsBuilder();
        builder.strengthPoints(10);
        player1.setPoints(builder.build());

        player2.getHand().addToHand(genericBuilding);
        player2.getHand().addToHand(genericBuilding);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        fraternalfeudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);
    }

    @Test
    public void manyCardsChosen() {
        String result = "";

        player1 = TestUtils.inizializePlayer(0);
        player2 = TestUtils.inizializePlayer(1);

        for (int i = 0; i < 5; i++) {
            player1.getHand().addToHand(genericBuilding);
            player2.getHand().addToHand(genericBuilding);
        }


        PointsBuilder builder = new PointsBuilder();
        builder.strengthPoints(10);
        player1.setPoints(builder.build());

        player1.messages.add("1 2");

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        fraternalfeudEvent.resolve(player1, player2);

        result += player1.printPrincipality() + "\n";
        result += player1.printHand() + "\n";
        result += player2.printPrincipality() + "\n";
        result += player2.printHand() + "\n";

        Approvals.verify(result);
    }

}
