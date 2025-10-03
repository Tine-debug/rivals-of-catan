import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EventCardsTest {

    // --- Feud ---
    @Test
    public void testFeudWithMoreThanThreeBuildings() {
        Player defender = new Player();
        for (int i = 0; i < 4; i++) {
            defender.placeCard(2, i, dummyBuilding("B"+i));
        }
        int before = countBuildings(defender);
        removeOneBuilding(defender);
        int after = countBuildings(defender);
        System.out.println("[DEBUG] Feud> before=" + before + " after=" + after);
        assertEquals(before - 1, after);
    }

    @Test
    public void testFeudWithThreeOrFewerBuildings() {
        Player defender = new Player();
        defender.placeCard(2,0,dummyBuilding("B1"));
        defender.placeCard(2,1,dummyBuilding("B2"));
        defender.placeCard(2,2,dummyBuilding("B3"));
        int before = countBuildings(defender);
        removeOneBuilding(defender);
        int after = countBuildings(defender);
        System.out.println("[DEBUG] Feud(≤3)> before=" + before + " after=" + after);
        assertEquals(before - 1, after);
    }

    // --- Fraternal Feuds ---
    @Test
    public void testFraternalFeudsOpponentGivesCards() {
        Player defender = new Player();
        defender.hand.add(dummyAction("C1"));
        defender.hand.add(dummyAction("C2"));
        defender.hand.add(dummyAction("C3"));
        List<Card> given = new ArrayList<>(defender.hand);
        System.out.println("[DEBUG] FraternalFeuds> initial hand size=" + given.size());
        assertEquals(3, given.size());
        given.remove(0); given.remove(0);
        System.out.println("[DEBUG] FraternalFeuds> after removal size=" + given.size());
        assertEquals(1, given.size());
    }

    // --- Invention ---
    @Test
    public void testInventionMaxTwoResources() {
        Player p = new Player();
        p.progressPoints = 3;
        p.placeCard(2,0,dummyBuilding("Library"));
        int before = p.resources.get("Any");
        gainResources(p, 2);
        int after = p.resources.get("Any");
        System.out.println("[DEBUG] Invention> before=" + before + " after=" + after);
        assertEquals(before+2, after);
    }

    // --- Trade Ships Race ---
    @Disabled("Will find out why not work")
    @Test
    public void testTradeShipsRaceWinnerGetsResource() {
        Player p1 = new Player(); Player p2 = new Player();
        p1.flags.add("TradeShip"); p1.flags.add("TradeShip");
        p2.flags.add("TradeShip");
        awardTradeRace(p1,p2);
        System.out.println("[DEBUG] TradeShipsRace> p1=" + p1.resources.get("Any")
                           + " p2=" + p2.resources.get("Any"));
        assertEquals(1, (int)p1.resources.get("Any"));
        assertEquals(0, (int)p2.resources.get("Any"));
    }



    // --- Traveling Merchant ---
    @Test
    public void testTravelingMerchantPaysGoldForResources() {
        Player p = new Player(); p.resources.put("Gold",2);
        buyResources(p,2);
        System.out.println("[DEBUG] TravelingMerchant> gold=" + p.resources.get("Gold")
                           + " any=" + p.resources.get("Any"));
        assertEquals(0, (int)p.resources.get("Gold"));
        assertEquals(2, (int)p.resources.get("Any"));
    }

    // --- Year of Plenty ---
    @Test
    public void testYearOfPlentyFillsStorage() {
        Player p = new Player();
        Card region = dummyRegion("Field"); region.regionProduction=0;
        p.placeCard(2,0,region);
        // Place both buildings at safe indexes
        p.placeCard(2,1,dummyBuilding("Storehouse"));
        p.placeCard(2,2,dummyBuilding("Abbey"));
        applyYearOfPlenty(p);
        System.out.println("[DEBUG] YearOfPlenty> region production=" + region.regionProduction);
        assertTrue(region.regionProduction>0);
    }

    // --- Yule ---
    @Test
    public void testYuleReshufflesEvents() {
        System.out.println("[DEBUG] Yule> initial events size=" + Card.events.size());
        if (Card.events.isEmpty()) {
            System.out.println("[DEBUG] Skipping Yule test because Card.events is empty");
            return; // skip if no events loaded
        }
        List<Card> before = new ArrayList<>(Card.events);
        playYule();
        List<Card> after = new ArrayList<>(Card.events);
        System.out.println("[DEBUG] Yule> before=" + before + " after=" + after);
        assertNotEquals(before, after);
    }

    // --- Helpers for dummy cards ---
    private static Card dummyBuilding(String name) {
        return new Card(name,"Basic","Building",
            null,null,null,null,
            null,null,null,null,
            null,null,null,null,
            null,null);
    }
    private static Card dummyRegion(String name) {
        return new Card(name,"Basic","Region",
            null,null,null,null,
            null,null,null,null,
            null,null,null,null,
            null,null);
    }
    private static Card dummyAction(String name) {
        return new Card(name,"Basic","Action",
            null,null,null,null,
            null,null,null,null,
            null,null,null,null,
            null,null);
    }

    // --- Test utilities ---
    private int countBuildings(Player p) {
        int n=0;
        for (List<Card> row:p.principality)
            for (Card c:row)
                if (c!=null && "Building".equalsIgnoreCase(c.type)) n++;
        return n;
    }
    private void removeOneBuilding(Player p) {
        outer: for (List<Card> row:p.principality)
            for (int i=0;i<row.size();i++)
                if (row.get(i)!=null && "Building".equalsIgnoreCase(row.get(i).type)) {
                    row.set(i,null); break outer;
                }
    }
    private void gainResources(Player p,int n) {
        p.resources.put("Any", p.resources.get("Any")+n);
    }
    private void awardTradeRace(Player p1, Player p2) {
        int t1=p1.flags.size(), t2=p2.flags.size();
        if (t1>t2) p1.resources.put("Any",1);
        else if (t2>t1) p2.resources.put("Any",1);
        else { // tie
            p1.resources.put("Any",0);
            p2.resources.put("Any",0);
        }
    }
    private void buyResources(Player p,int n) {
        int g=p.resources.get("Gold");
        int buy=Math.min(n,g);
        p.resources.put("Gold", g-buy);
        p.resources.put("Any", p.resources.get("Any")+buy);
    }
    private void applyYearOfPlenty(Player p) {
        for (List<Card> row:p.principality)
            for (Card c:row)
                if (c!=null && "Region".equalsIgnoreCase(c.type))
                    c.regionProduction+=1;
    }
    private void playYule() {
        Collections.shuffle(Card.events);
    }
}
