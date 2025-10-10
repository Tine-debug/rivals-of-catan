import java.util.Vector;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlayerApprovalTest {


    Player player;

    @BeforeAll
    static void setupall() {
        try {
            Vector<Card> BasicCards = Card.loadThemeCards("cards.json", "basic", false);
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
        Card forest = new Card();
        forest.name = "Forest";
        forest.type = "Region";
        forest.regionProduction = 2;
        Card settlement = new Card();
        settlement.name = "Settlement";
        settlement.type = "Road";
        settlement.victoryPoints = "2";

        player.placeCard(0, 0, forest);
        player.placeCard(1, 1, settlement);

        Card cardInHand = new Card();
        cardInHand.name = "Mill";
        cardInHand.type = "Building";
        cardInHand.cost = "2 Grain";
        cardInHand.SP = "1";
        player.addToHand(cardInHand);

        Approvals.verify(player.printPrincipality() + "\n" + player.printHand());
    }

    @Test
    void resourceManipulation() {
        Card forest1 = new Card();
        forest1.name = "Forest";
        forest1.type = "Region";
        forest1.regionProduction = 0;
        Card forest2 = new Card();
        forest2.name = "Forest";
        forest2.type = "Region";
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
        player.victoryPoints = 3;
        player.commercePoints = 5;
        player.strengthPoints = 2;

        opp.victoryPoints = 2;
        opp.commercePoints = 1;
        opp.strengthPoints = 0;

        String scoreReport = "Player score vs opponent: " + player.currentScoreAgainst(opp) +
                "\nHas trade token: " + player.hasTradeTokenAgainst(opp) +
                "\nHas strength token: " + player.hasStrengthTokenAgainst(opp);

        Approvals.verify(scoreReport);
    }

    @Test
    void expandAfterEdgeBuild() {
        Card c = new Card();
        c.name = "Forest";
        player.placeCard(0,0,c);
        int newColLeft = player.expandAfterEdgeBuild(0); // triggers left expansion
        int newColRight = player.expandAfterEdgeBuild(player.principality.get(0).size()-1); // triggers right expansion
        Approvals.verify("Left col now: " + newColLeft + ", Right col now: " + newColRight);
    }

    @Test
    void hasInPrincipalityCheck() {
        Card c = new Card();
        c.name = "Mine";
        player.placeCard(2,2,c);
        String report = "Has Mine: " + player.hasInPrincipality("Mine") +
                        ", Has Forest: " + player.hasInPrincipality("Forest");
        Approvals.verify(report);
    }

    @Test
    void removeFromHandByNameTest() {
        Card c1 = new Card();
        c1.name = "Card1";
        Card c2 = new Card();
        c2.name = "Card2";
        player.addToHand(c1);
        player.addToHand(c2);

        Card removed = player.removeFromHandByName("Card1");
        Card notFound = player.removeFromHandByName("Unknown");

        Approvals.verify("Removed: " + removed.name + ", NotFound: " + notFound);
    }

    @Test
    void gainAnyResourceWithPrompt() {
        Player promptPlayer = new Player() {
            @Override
            public String receiveMessage() {
                return "Brick"; // simulate choosing a resource
            }
        };

        Card hill = new Card();
        hill.name = "Hill";
        hill.type = "Region";
        hill.regionProduction = 0;
        promptPlayer.placeCard(0,0,hill);

        promptPlayer.gainResource("Any");
        Approvals.verify("Brick count: " + promptPlayer.getResourceCount("Brick"));
    }
}
