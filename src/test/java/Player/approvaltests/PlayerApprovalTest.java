import java.util.Vector;

import Card.*;
import Player.*;
import Turns.*;
import Points.*;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlayerApprovalTest {


    Player player;
    private static Cardstacks stacks = Cardstacks.getInstance();

    @BeforeAll
    static void setupall() {
        try {
            Vector<Card> BasicCards = stacks.loadThemeCards("cards.json", "basic", false);
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
        Card hill = new Card("Hill", null, "Region", null, "Center Card", null, null, 
        null, null, null, null, null, null, null, "Settlement built" , null, null);
        hill.regionProduction = 1;
        player.placeCard(0, 0, hill);
        Approvals.verify(player.printPrincipality());
    }

    @Test
    void addMultipleCardsAndHand() {
        Card forest = new Card("Forest", "Region", null);
        forest.regionProduction = 2;
        Card settlement = new Card("Settlement", "Road", null);
        //String victoryPoints, String CP, String SP, String FP, String PP, String LP, String KP
        settlement.updatePoints(new Points("2", null, null, null, null, null, null));        

        player.placeCard(0, 0, forest);
        player.placeCard(1, 1, settlement);

        Card cardInHand = new Card("Mill", "Building", "2 Grain");
        cardInHand.updatePoints(new Points(null, null, "1", null, null, null, null));
        player.addToHand(cardInHand);

        Approvals.verify(player.printPrincipality() + "\n" + player.printHand());
    }

    @Test
    void resourceManipulation() {
        Card forest1 = new Card("Forest", "Region", null);
        forest1.regionProduction = 0;
        Card forest2 = new Card("Forest", "Region", null);
        forest2.regionProduction = 1;

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
        player.points.victoryPoints = 3;
        player.points.commercePoints = 5;
        player.points.strengthPoints = 2;

        opp.points.victoryPoints = 2;
        opp.points.commercePoints = 1;
        opp.points.strengthPoints = 0;

        String scoreReport = "Player score vs opponent: " + player.currentScoreAgainst(opp) +
                "\nHas trade token: " + player.hasTradeTokenAgainst(opp) +
                "\nHas strength token: " + player.hasStrengthTokenAgainst(opp);

        Approvals.verify(scoreReport);
    }

    @Test
    void expandAfterEdgeBuild() {
        Card c = new Card("Forest", null, null);
        player.placeCard(0,0,c);
        int newColLeft = player.expandAfterEdgeBuild(0); // triggers left expansion
        int newColRight = player.expandAfterEdgeBuild(player.principality.principality.get(0).size()-1); // triggers right expansion
        Approvals.verify("Left col now: " + newColLeft + ", Right col now: " + newColRight);
    }

    @Test
    void hasInPrincipalityCheck() {
        Card c = new Card("Mine", null, null);
        player.placeCard(2,2,c);
        String report = "Has Mine: " + player.hasInPrincipality("Mine") +
                        ", Has Forest: " + player.hasInPrincipality("Forest");
        Approvals.verify(report);
    }

    @Test
    void removeFromHandByNameTest() {
        Card c1 = new Card("Card1", null, null);
        Card c2 = new Card("Card2", null, null);
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

        Card hill = new Card("Hill", "Region", null);
        hill.regionProduction = 0;
        promptPlayer.placeCard(0,0,hill);

        promptPlayer.gainResource("Any");
        Approvals.verify("Brick count: " + promptPlayer.getResourceCount("Brick"));
    }
}
