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


public class PrincipialityinionePlayerApprovalTest{
    Player player;

    int center = 2;
    int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };

    @BeforeAll
    public static void setupall(){
        try {
            Cardstacks.loadBasicCards("cards.json");
            }    catch (Exception e) {
            System.err.println("Failed to load");
            }
    }

    @BeforeEach
    void setup(){
        player = new Player();
    }


    @Test
    void principalityinitoineplayertest1(){
        Server.pricipalityinitoneplayer(player, regionDice, center, 1);
        Approvals.verify(player.principality.printPrincipality(player));
    }

    @Test
    void principalityinitoineplayertest0(){
        Server.pricipalityinitoneplayer(player, regionDice, center, 0);
        Approvals.verify(player.principality.printPrincipality(player));
    }





}