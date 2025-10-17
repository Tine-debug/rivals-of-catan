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



public class CardApplyEffectApprovalTest {

    /*static class MockPlayer extends Player {
    @Override
    public void sendMessage(Object msg) {}

    @Override
    public String receiveMessage() { return "Brick"; }

}
*/


    static Vector<Card> basicCards;
    MockPlayer player1;
    MockPlayer player2;
    MockPlayer player3;
    static Vector<int[]> interestingFields = new Vector<>();

    static void resetStack(){
        try {
            Cardstacks.loadBasicCards("cards.json");
            }    catch (Exception e) {
            System.err.println("Failed to load");
            }
    }

    static void resetBasicCards(){
        try {
            basicCards = Cardstacks.loadThemeCards("cards.json", "basic", false);
        } catch (Exception e) {
            System.err.println("Failed to load");
        }
    }

    @BeforeEach
    void reset(){
        resetStack();
    }

    @BeforeAll
    static void setupCardsandFields() {
        resetStack();
        resetBasicCards();
            for (int i = 0; i<5 ; i++){
                for (int j = 0; j<6 ; j++){
                    interestingFields.add(new int[]{i, j});
                }
            }
             interestingFields.add(new int[]{0,1});
             interestingFields.add(new int[]{0,3});

        }
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


    
    Card getCard(String name){
        resetBasicCards();
                Card res = Cardstacks.popCardByName(basicCards, name);
        resetBasicCards();
        return res;

    }



    void setupPlayer1(){
        resetStack();
        player1 = new MockPlayer();
        player1.points.commercePoints = 3;
        player1.points.progressPoints = 3;
        player1.points.skillPoints = 3;
        player1.points.strengthPoints = 3;
        int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };
        Server.pricipalityinitoneplayer(player1, regionDice, 2,  1);
        player1.gainResource("Gold");
        player1.gainResource("Gold");
        player1.gainResource("Gold");
        player1.gainResource("Wool");

    }

     void setupPlayer2 (){

            resetStack();
                player2 = new MockPlayer();
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

        @Test
        void testCardApplyEffectRoadSettlement () {
            String result = "";
            setupPlayer1();

           Card road = getCard("Road");
            for (int j = 0; j < interestingFields.size(); j++)
            {
                result = result + "\n at: " + interestingFields.get(j)[0] + "," + interestingFields.get(j)[1] +  "\n";
                result = result + "\n"+ String.valueOf(road.applyEffect(player1, player1, interestingFields.get(j)[0], interestingFields.get(j)[1]));
                result = result + "\n" + player1.printPrincipality();
                result = result + "\n" + player1.flags.toString();
            
            
            }
            Card set = getCard("Settlement");
             for (int j = 0; j < interestingFields.size(); j++)
            {
            result = result + "\n at: " + interestingFields.get(j)[0] + "," + interestingFields.get(j)[1] +  "\n";  
            result = result + "\n"+ String.valueOf(set.applyEffect(player1, player1, interestingFields.get(j)[0], interestingFields.get(j)[1]));
            result = result + "\n" + player1.printPrincipality();
            result = result + "\n" + player1.flags.toString();
            
            }
    

            Approvals.verify(result);
        }

        @Test
        void MerchandCaravannoRessources(){
            String result = "";
            setupPlayer2();
            player2.removeResource("Brick", 1);
            player2.removeResource("Wool", 1);
            player2.removeResource("Lumber", 1);
            player2.removeResource("Grain", 1);
            player2.removeResource("Ore", 1);


            Card merchand = getCard("Merchant Caravan");
            result = result + String.valueOf(merchand.applyEffect(player2, player2, 0, 0));
            result = result + "\n" + player2.printPrincipality();
            result = result + "\n" + player2.flags.toString();
            Approvals.verify(result);

        }


    static class MockPlayer extends Player{
        public Vector<String[]> messages = new Vector();
        public int messagenumber = 0;
        public int messageblock = 0;
        @Override
        public String receiveMessage() { 
            if (messages.size() == 0) return "Brick";
            String message = "";
            try {
                message = messages.get(messageblock)[messagenumber];
                if (messagenumber<2) messagenumber++;
                else{
                    messagenumber = 0;
                    messageblock++;
                }
            } catch (Exception e) {
                
            }
            
            return message; }
        
    }


    @ParameterizedTest
    @ValueSource(strings = {"Region", "EXP"})
    void applyEffectswap(String nameChanged){
        resetStack();
        player3 = new MockPlayer();
        int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };
        Server.pricipalityinitoneplayer(player3, regionDice, 2,  1);

        Card brickfactory = getCard("Brick Factory");
        Card harald =  getCard("Harald");
        Card reloc = getCard("Relocation");

        for (int i = 0; i < 5; i++){
            for (int j = 0; j < 5; j++){
                String[] newBlock = {nameChanged, String.format("%d %d", i, j), String.format("%d %d", i + 2, j)};
                player3.messages.add(newBlock);
                if (i<2)brickfactory.applyEffect(player3, player3, i, j);
                else harald.applyEffect(player3, player3, i, j);
            }   
        } 
        player3.messages.add(new String[]{nameChanged, "x", "1 0"});
        player3.messages.add(new String[]{nameChanged, "1 0", "x"});
        String result = "";
        for (int i = 0; i < player3.messages.size(); i++){
            result = result + "\n" + player3.printPrincipality();
            result = result + "\n" + List.of(player3.messages.get(i));
            result = result + "\n" + String.valueOf(reloc.applyEffect(player3, player3, 0, 0));
            result = result + "\n" + player3.printPrincipality();
            result = result + "\n" + player3.flags.toString();
            result = result + "\n ====================================";
            player3.flags.clear();
        }

        Approvals.verify(result, Approvals.NAMES.withParameters(nameChanged));

    }


    @Test
    void testScout(){

        Card scout = getCard("scout");
        Card road = getCard("road");
        Card settlement = getCard("Settlement");

    //add messages
    Vector<String[]> testmessages = new Vector();
    testmessages.add(new String[]{"1", "0"});
    testmessages.add(new String[]{"0", "1"});
    testmessages.add(new String[]{null, "1"});
    testmessages.add(new String[]{"0", null});
    String result = "";

    for (int i = 0; i < testmessages.size(); i++){
        resetStack();
        resetBasicCards();
        player3 = new MockPlayer();
        int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };
        Server.pricipalityinitoneplayer(player3, regionDice, 2,  1);
        player3.messages.add(testmessages.get(i));
        result = result + "\n" + "===============================================";
        result = result + "\n" + player3.printPrincipality();
        road.applyEffect(player3, player3, 2, 0);
        result = result + "\n" + player3.printPrincipality();
        scout.applyEffect(player3, player3, 0, 0);
        result = result + "\n" + player3.flags.toString();
        result = result + "\n" + String.valueOf(settlement.applyEffect(player3, player3, 2, 0));
        result = result + "\n" + player3.flags.toString();
        result = result + "\n" + player3.printPrincipality();
        result = result + "\n" + "===============================================";
    }

    Approvals.verify(result);

        



    }

    

}
