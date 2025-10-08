import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeAll;
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
    static Vector<int[]> interestingFields = new Vector<>();


    @BeforeAll
    static void setupCardsandFields() {
        try {
            basicCards = Card.loadThemeCards("cards.json", "basic", false);
            Card.loadBasicCards("cards.json");
        } catch (Exception e) {
            System.err.println("Failed to load");
        }
            for (int i = 0; i<5 ; i++){
                for (int j = 0; j<6 ; j++){
                    interestingFields.add(new int[]{i, j});
                }
            }
             interestingFields.add(new int[]{0,1});
             interestingFields.add(new int[]{0,3});


        }



    void setupPlayer1(){
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

    }

     void setupPlayer2 (){
        try {
                    Card.loadBasicCards("cards.json");
                } catch (Exception e) {
                    System.err.println("Failed to load");
                }
                player2 = new StubPlayer();
                int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };
                Server.pricipalityinitoneplayer(player2, regionDice, 2,  1);


    }



    static Stream<Arguments> cardTestProviderwithindex() {
    List<Arguments> args = new ArrayList<>();
    for (int i = 0; i < basicCards.size(); i++) {
        Card card = basicCards.get(i);
            args.add(Arguments.of( card.name, i, card));
    }
    return args.stream();
    }


        @ParameterizedTest(name = "[{index}]-p2p2{0}_individual")
        @MethodSource("cardTestProviderwithindex")
        void testCardApplyEffectoneatIndividuallyp2p2 (String cardname,int i, Card card) {
            String result = "";
            setupPlayer2();
            for (int j = 0; j < interestingFields.size(); j++)
            {
                result = result + "\n at: " + interestingFields.get(j)[0] + "," + interestingFields.get(j)[1] + "\n";
                boolean effectResult = card.applyEffect(player2, player2, interestingFields.get(j)[0], interestingFields.get(j)[1]);
                result = result + String.valueOf(effectResult);
                result = result + "\n" + player2.printPrincipality();
                result = result + "\n" + player2.flags.toString();
            }
            Approvals.verify(result, Approvals.NAMES.withParameters(i + cardname));
        }

        @ParameterizedTest(name = "[{index}]-p2p1{0}_individual")
        @MethodSource("cardTestProviderwithindex")
        void testCardApplyEffectoneatIndividuallyp2p1 (String cardname,int i, Card card) {
            String result = "";
            setupPlayer1();
            setupPlayer2();
            for (int j = 0; j < interestingFields.size(); j++)
            {
                result = result + "\n at: " + interestingFields.get(j)[0] + "," + interestingFields.get(j)[1] +  "\n";
                boolean effectResult = card.applyEffect(player2, player1, interestingFields.get(j)[0], interestingFields.get(j)[1]);
                result = result + String.valueOf(effectResult);
                result = result + "\n" + player1.printPrincipality() + "\n" + player2.printPrincipality();
                result = result + "\n" + player1.flags.toString() + "\n" + player2.flags.toString();
            }
            Approvals.verify(result, Approvals.NAMES.withParameters(i + cardname));
        }


        @ParameterizedTest(name = "[{index}]-p1p2{0}_individual")
        @MethodSource("cardTestProviderwithindex")
        void testCardApplyEffectp1p2 (String cardname,int i, Card card) {
            String result = "";
            setupPlayer1();
            setupPlayer2();
            for (int j = 0; j < interestingFields.size(); j++)
            {
                result = result + "\n at: " + interestingFields.get(j)[0] + "," + interestingFields.get(j)[1] +  "\n";
                boolean effectResult = card.applyEffect(player1, player2, interestingFields.get(j)[0], interestingFields.get(j)[1]);
                result = result + String.valueOf(effectResult);
                result = result + "\n" + player1.printPrincipality() + "\n" + player2.printPrincipality();
                result = result + "\n" + player1.flags.toString() + "\n" + player2.flags.toString();
            }
            Approvals.verify(result, Approvals.NAMES.withParameters(i + cardname));
        }


       @ParameterizedTest(name = "[{index}]-p1p1{0}_individual")
        @MethodSource("cardTestProviderwithindex")
        void testCardApplyEffectp1p1 (String cardname,int i, Card card) {
            String result = "";
            setupPlayer1();
            for (int j = 0; j < interestingFields.size(); j++)
            {
                result = result + "\n at: " + interestingFields.get(j)[0] + "," + interestingFields.get(j)[1] +  "\n";
                boolean effectResult = card.applyEffect(player1, player1, interestingFields.get(j)[0], interestingFields.get(j)[1]);
                result = result + String.valueOf(effectResult);
                result = result + "\n" + player1.printPrincipality();
                result = result + "\n" + player1.flags.toString();
            }
            Approvals.verify(result, Approvals.NAMES.withParameters(i + cardname));
        }


    

}
