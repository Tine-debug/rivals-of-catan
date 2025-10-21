import java.util.Vector;

import Card.*;
import Player.*;
import Turns.*;
import Points.*;
import Card.Cardstack.CardstackFacade;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

public class PlayerApprovalTest {


    Player player;
    private static CardstackFacade stacks = CardstackFacade.getInstance();

    @BeforeAll
    static void setupall() {
        try {
            List<Card> BasicCards = stacks.loadCardsForTesting("cards.json", "basic", false, true);
        } catch (Exception e) {
        }
            System.err.println("Failed to load: ");
        }

    @BeforeEach
    void setupeach() {
        player = new Player();
    }

    @Test
    void emptyPrincipality() {
        Approvals.verify(player.printPrincipality());
    }

    @Test
    void addCardToPrincipality() {
        Cardbuilder cardbuilder = new Cardbuilder();
        cardbuilder.name("Hill");
        cardbuilder.type("Region");
        cardbuilder.placement("Center Card");
        Card hill = cardbuilder.build();
        hill.setRegionProduction(1);
        player.placeCard(0, 0, hill);
        Approvals.verify(player.printPrincipality());
    }


/*
    @Test
    void addMultipleCardsAndHand() {
        Card forest = new Card("Forest", "Region", null);
        forest.setRegionProduction(2);
        Card settlement = new Card("Settlement", "Road", null);
        //String victoryPoints, String CP, String SP, String FP, String PP, String LP, String KP
        settlement.setPoints(new Points("2", null, null, null, null, null, null));        

        player.placeCard(0, 0, forest);
        player.placeCard(1, 1, settlement);

        Card cardInHand = new Card("Mill", "Building", "2 Grain");
        cardInHand.setPoints(new Points(null, null, "1", null, null, null, null));
        player.addToHand(cardInHand);

        Approvals.verify(player.printPrincipality() + "\n" + player.printHand());
    }
*/

    @Test
    void addMultipleCardsAndHand() {
        Cardbuilder cardbuilder = new Cardbuilder();


        cardbuilder.name("Forest");
        cardbuilder.type("Region");
        Card forest = cardbuilder.build();
        forest.setRegionProduction(2);

        cardbuilder = new Cardbuilder();
        cardbuilder.name("Settlement");
        cardbuilder.type("Road");
        PointsBuilder pointsBuilder = new PointsBuilder();
        pointsBuilder.victoryPoints(2);
        cardbuilder.points(pointsBuilder.build());
        Card settlement = cardbuilder.build();
        player.placeCard(0, 0, forest);
        player.placeCard(1, 1, settlement);


        cardbuilder = new Cardbuilder();
        pointsBuilder = new PointsBuilder();
        cardbuilder.name("Mill");
        cardbuilder.type("Building");
        cardbuilder.cost("2 Grain");

        pointsBuilder.strengthPoints(1);
        cardbuilder.points(pointsBuilder.build());
        Card cardInHand = cardbuilder.build();
        player.addToHand(cardInHand);

        Approvals.verify(player.printPrincipality() + "\n" + player.printHand());
    }

    @Test
    void resourceManipulation() {
        Cardbuilder cardbuilder = new Cardbuilder();
        cardbuilder.name("Forest");
        cardbuilder.type("Region");
        Card forest1 = cardbuilder.build();
        forest1.setRegionProduction(0);
        Card forest2 = cardbuilder.build();
        forest2.setRegionProduction(1);

        player.placeCard(0,0,forest1);
        player.placeCard(0,1,forest2);

        player.gainResource("Lumber"); // adds 1 to lowest
        player.gainResource("Lumber"); // adds 1 to the same or next lowest
        player.removeResource("Lumber", 1); // removes highest
        player.setResourceCount("Lumber", 3); // redistribute to total 3
        player.setResourceCount("Lumber", 1); // reduce to 1

        Approvals.verify("Total Lumber: " + player.getResourceCount("Lumber"));
    }

    @Test
    void scoreAndTokens() {
        Player opp = new Player();
        player.getPoints().victoryPoints = 3;
        player.getPoints().commercePoints = 5;
        player.getPoints().strengthPoints = 2;

        opp.getPoints().victoryPoints = 2;
        opp.getPoints().commercePoints = 1;
        opp.getPoints().strengthPoints = 0;

        String scoreReport = "Player score vs opponent: " + player.currentScoreAgainst(opp) +
                "\nHas trade token: " + player.hasTradeTokenAgainst(opp) +
                "\nHas strength token: " + player.hasStrengthTokenAgainst(opp);

        Approvals.verify(scoreReport);
    }

    @Test
    void expandAfterEdgeBuild() {
        Cardbuilder cardbuilder = new Cardbuilder();
        cardbuilder.name("Forest");
        Card c = cardbuilder.build();
        player.placeCard(0,0,c);
        int newColLeft = player.expandAfterEdgeBuild(0); // triggers left expansion
        int newColRight = player.expandAfterEdgeBuild(player.getPrincipality().principality.get(0).size()-1); // triggers right expansion
        Approvals.verify("Left col now: " + newColLeft + ", Right col now: " + newColRight);
    }

    @Test
    void hasInPrincipalityCheck() {
        Cardbuilder cardbuilder = new Cardbuilder();
        cardbuilder.name("Mine");
        Card c = cardbuilder.build();
        player.placeCard(2,2,c);
        String report = "Has Mine: " + player.hasInPrincipality("Mine") +
                        ", Has Forest: " + player.hasInPrincipality("Forest");
        Approvals.verify(report);
    }

    @Test
    void removeFromHandByNameTest() {
        Cardbuilder cardbuilder = new Cardbuilder();
        cardbuilder.name("Card1");
        Card c1 = cardbuilder.build();
        cardbuilder.name("Card2");
        Card c2 = cardbuilder.build();
        player.addToHand(c1);
        player.addToHand(c2);

        Card removed = player.removeFromHandByName("Card1");
        Card notFound = player.removeFromHandByName("Unknown");

        Approvals.verify("Removed: " + removed.toString()+ ", NotFound: " + notFound);
    }

    @Test
    void gainAnyResourceWithPrompt() {
        Player promptPlayer = new Player() {
            @Override
            public String receiveMessage() {
                return "Brick"; // simulate choosing a resource
            }
        };
        Cardbuilder cardbuilder = new Cardbuilder();
        cardbuilder.name("Hill");
        cardbuilder.type("Region");
        Card hill = cardbuilder.build();
        hill.setRegionProduction(0);
        promptPlayer.placeCard(0,0,hill);

        promptPlayer.gainResource("Any");
        Approvals.verify("Brick count: " + promptPlayer.getResourceCount("Brick"));
    }
}
