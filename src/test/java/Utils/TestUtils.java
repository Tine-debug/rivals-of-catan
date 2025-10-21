package Utils;

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

public class TestUtils {

static private PrintStream originalOut;
static private PrintStream originalErr;

private static CardstackFacade stacks = CardstackFacade.getInstance();

static public void suppressOutput() {
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
    }

static public void restoreOutput() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

public static MockPlayer inizializePlayer(int idx){
    MockPlayer player = new MockPlayer();
    int center = 2;
    int[][] regionDice = {{2, 1, 6, 3, 4, 5}, {3, 4, 5, 2, 1, 6}};
    try {
        stacks.loadCardsForTesting("cards.json", "basic", false, true);
    } catch (Exception e) {
    }
    stacks.initializePlayerPrincipality(player, regionDice, center, idx);
    return player;
}


}
