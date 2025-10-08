import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;



public class CardApplyEffectApprovalTest {

    static class StubPlayer extends Player {
    @Override
    public void sendMessage(Object msg) {}

    @Override
    public String receiveMessage() { return "Brick"; }

}

    static Vector<Card> basicCards;
    StubPlayer player1;
    StubPlayer player2;
    static int[][] interestingFields = {{1,0},{1,1},{1,2},{2,1},{2,2},{3,2},{2,0}};


    @BeforeAll
    static void setupall() {
        try {
            basicCards = Card.loadThemeCards("cards.json", "basic");
            Card.loadBasicCards("cards.json");
        } catch (Exception e) {
        }
            System.err.println("Failed to load");
        }

    @BeforeEach
    void setupeach() {
        try {
            Card.loadBasicCards("cards.json");
        } catch (Exception e) {
            System.err.println("Failed to load");
        }
        player1 = new StubPlayer();
        player1.commercePoints = 3;
        player1.progressPoints = 3;
        player1.skillPoints = 3;
        player1.strengthPoints = 3;
        player1.gainResource("Gold");
        player1.gainResource("Gold");
        player1.gainResource("Wool");
        int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };
        Server.pricipalityinitoneplayer(player1, regionDice, 2,  1);
        try {
            Card.loadBasicCards("cards.json");
        } catch (Exception e) {
            System.err.println("Failed to load");
        }
    }


    /*@ParameterizedTest
    @
    void applyEffectPlayer2Player2Test(){
        for (int i = 0; i < basicCards.size(); i++){
            player2 = new Player();
            int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };
            Server.pricipalityinitoneplayer(player2, regionDice, 2,  1);
            for (int j = 0; j < 6; j++){
                String result = String.valueOf(basicCards.get(i).applyEffect(player2, player2, interestingFields[j][0], interestingFields[j][1]));
                Approvals.verify(
                     result);
            }

        }
        }

        */


    static Stream<Arguments> cardTestProvider() {
    List<Arguments> args = new ArrayList<>();
    for (int i = 0; i < basicCards.size(); i++) {
        Card card = basicCards.get(i);
        for (int j = 0; j < interestingFields.length; j++) {
            args.add(Arguments.of( card.name, i, j, card));
        }
    }
    return args.stream();
    }

        @ParameterizedTest(name = "[{index}]-{0}_at_{1}-{2}")
        @MethodSource("cardTestProvider")
        void testCardApplyEffectoneatIndividuallyp2p2 (String cardname,int i, int j, Card card) {
            player2 = new StubPlayer();
            int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };
            Server.pricipalityinitoneplayer(player2, regionDice, 2,  1);
            boolean effectResult = card.applyEffect(player2, player2, interestingFields[j][0], interestingFields[j][1]);
            String result = String.valueOf(effectResult);
            result = result + "\n" + player2.printPrincipality();
            Approvals.verify(result, Approvals.NAMES.withParameters(cardname + i + "at" + interestingFields[j][0] + "," + interestingFields[j][1]));
        }

    

}
