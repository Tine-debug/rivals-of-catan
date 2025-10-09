import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;



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
        int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };
        Server.pricipalityinitoneplayer(player1, regionDice, 2,  1);
        player1.gainResource("Gold");
        player1.gainResource("Gold");
        player1.gainResource("Gold");
        player1.gainResource("Wool");

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

        @Test
        void testCardApplyEffectRoadSettlement () {
            String result = "";
            setupPlayer1();
             try {
            basicCards = Card.loadThemeCards("cards.json", "basic", false);
            }    catch (Exception e) {
            System.err.println("Failed to load");
        }
           Card road = Card.popCardByName(basicCards, "Road");
            for (int j = 0; j < interestingFields.size(); j++)
            {
                result = result + "\n at: " + interestingFields.get(j)[0] + "," + interestingFields.get(j)[1] +  "\n";
                result = result + "\n"+ String.valueOf(road.applyEffect(player1, player1, interestingFields.get(j)[0], interestingFields.get(j)[1]));
                result = result + "\n" + player1.printPrincipality();
                result = result + "\n" + player1.flags.toString();
            
            
            }
            Card set = Card.popCardByName(basicCards, "Settlement");
             for (int j = 0; j < interestingFields.size(); j++)
            {
            result = result + "\n at: " + interestingFields.get(j)[0] + "," + interestingFields.get(j)[1] +  "\n";  
            result = result + "\n"+ String.valueOf(set.applyEffect(player1, player1, interestingFields.get(j)[0], interestingFields.get(j)[1]));
            result = result + "\n" + player1.printPrincipality();
            result = result + "\n" + player1.flags.toString();
            
            }
            try {
            basicCards = Card.loadThemeCards("cards.json", "basic", false);
            }    catch (Exception e) {
            System.err.println("Failed to load");
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

            try {
            basicCards = Card.loadThemeCards("cards.json", "basic", false);
            }    catch (Exception e) {
            System.err.println("Failed to load");
            }
            Card merchand = Card.popCardByName(basicCards, "Merchant Caravan");
            result = result + String.valueOf(merchand.applyEffect(player2, player2, 0, 0));
            result = result + "\n" + player2.printPrincipality();
            result = result + "\n" + player2.flags.toString();
            Approvals.verify(result);
             try {
            basicCards = Card.loadThemeCards("cards.json", "basic", false);
            }    catch (Exception e) {
            System.err.println("Failed to load");
            }

        }


    static class SwapPlayer extends StubPlayer{
        public Vector<String[]> messages = new Vector();
        public int messagenumber = 0;
        public int messageblock = 0;
        @Override
        public String receiveMessage() { 
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
                try {
                Card.loadBasicCards("cards.json");
                } catch (Exception e) {
                    System.err.println("Failed to load");
                }
        SwapPlayer player3 = new SwapPlayer();
        int[][] regionDice = { { 2, 1, 6, 3, 4, 5 }, { 3, 4, 5, 2, 1, 6 } };
        Server.pricipalityinitoneplayer(player3, regionDice, 2,  1);

        try {
            basicCards = Card.loadThemeCards("cards.json", "basic", false);
            }    catch (Exception e) {
            System.err.println("Failed to load");
            }
        Card brickfactory = Card.popCardByName(basicCards, "Brick Factory");
        Card harald = Card.popCardByName(basicCards, "Harald");
        Card reloc = Card.popCardByName(basicCards, "Relocation");

        try {
            basicCards = Card.loadThemeCards("cards.json", "basic", false);
            }    catch (Exception e) {
            System.err.println("Failed to load");
            }




        for (int i = 0; i < 5; i++){
            for (int j = 0; j < 5; j++){
                String[] newBlock = {nameChanged, String.format("%d %d", i, j), String.format("%d %d", i + 2, j)};
                player3.messages.add(newBlock);
                if (i<2)brickfactory.applyEffect(player3, player3, i, j);
                else harald.applyEffect(player3, player3, i, j);
            }   
        } 
        String result = "";
        for (int i = 0; i < player3.messages.size(); i++){
            result = result + "\n" + player3.printPrincipality();
            result = result + "\n" + List.of(player3.messages.get(i));
            result = result + "\n" + String.valueOf(reloc.applyEffect(player3, player3, 0, 0));
            result = result + "\n" + player3.printPrincipality();
            result = result + "\n" + player3.flags.toString();
            result = result + "\n ====================================";
        }

        Approvals.verify(result, Approvals.NAMES.withParameters(nameChanged));

    }

    

}
