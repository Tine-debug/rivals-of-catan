import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CardTest {

    private Card road, settlement;

    @BeforeEach
    void setup() {
        road = new Card("Road", "Basic", "Center", "Strasse", "Center",
                null, "1", "0", "0", "0", "0", "0", "0", "0", null,
                "Build a road", null);
        settlement = new Card("Settlement", "Basic", "Center", "Dorf", "Center",
                null, "1", "1", "0", "0", "0", "0", "0", "0", null,
                "Build a settlement", null);
    }

    @Test
    void testToStringAndCompareTo() {
        assertEquals("Road", road.toString());
        assertTrue(road.compareTo(settlement) < 0);
    }

    @Test
    void testNmEqualsAndNmAt() {
        assertTrue(Card.nmEquals("abc", "ABC"));
        assertFalse(Card.nmEquals("abc", null));
        assertTrue(Card.nmAt(road, "Road", "City"));
        assertFalse(Card.nmAt(null, "a", "b"));
    }

    @Test
    void testAsInt() {
        assertEquals(5, Card.asInt("5", 0));
        assertEquals(0, Card.asInt(null, 0));
        assertEquals(7, Card.asInt("bad", 7));
    }

    @Test
    void testPopCardByName() {
        Vector<Card> v = new Vector<>();
        v.add(road);
        Card out = Card.popCardByName(v, "road");
        assertEquals(road, out);
        assertTrue(v.isEmpty());
    }

    @Test
    void testExtractCardsByAttribute() {
        Vector<Card> v = new Vector<>();
        v.add(road);
        v.add(settlement);
        Vector<Card> roads = Card.extractCardsByAttribute(v, "name", "Road");
        assertEquals(1, roads.size());
        assertEquals("Road", roads.firstElement().name);
    }

    @Test
    void testBuildingBoostsRegion() {
        assertTrue(Card.buildingBoostsRegion("Iron Foundry", "Mountain"));
        assertFalse(Card.buildingBoostsRegion("Mill", "Mountain"));
    }
    @Disabled
    @Test
    void testScoutSettlementUsesPickRegion() {
        Card.regions.clear();
        Card.regions.add(new Card("Forest","Basic","Region",null,null,null,null,
                null,null,null,null,null,null,null,null,null,null));
        Card.regions.add(new Card("Field","Basic","Region",null,null,null,null,
                null,null,null,null,null,null,null,null,null,null));

        // Player has SCOUT flag set (so placeTwoDiagonalRegions will call pickRegionFromStackByNameOrIndex)
        MockPlayer p = new MockPlayer();
        p.flags.add("SCOUT_NEXT_SETTLEMENT");
        p.setResponses("Forest","Field","T"); // choose regions by name, then "Top" for placement

        Card settlement = new Card("Settlement","Basic","Center",null,"Center",
                null,"1",null,null,null,null,null,null,null,null,null,null);
        Card road = new Card("Road","Basic","Center",null,"Center",null,"1",
                null,null,null,null,null,null,null,null,null,null);

        // put a road so settlement can be placed
        p.placeCard(2,0,road);

        assertTrue(settlement.applyEffect(p,null,2,1));
        // after placement, player should have placed Forest/Field diagonally
        assertNotNull(p.getCard(1,0)); 
        assertNotNull(p.getCard(3,0));
        assertTrue(p.messages.stream().anyMatch(m -> m.contains("New settlement regions")));
    }


    @Test
    void testLoadBasicCards() throws Exception {
        String json = "[{\"name\":\"Road\",\"theme\":\"Basic\",\"type\":\"Center\"}," +
                      "{\"name\":\"Settlement\",\"theme\":\"Basic\",\"type\":\"Center\"}]";
        Path temp = Files.createTempFile("cards", ".json");
        Files.writeString(temp, json);
        Card.loadBasicCards(temp.toString());
        assertFalse(Card.roads.isEmpty());
        assertFalse(Card.settlements.isEmpty());
        Files.delete(temp);
    }

    @Disabled
    @Test
    void testApplyEffectRoad() {
        MockPlayer p = new MockPlayer();
        assertTrue(road.applyEffect(p, null, 2, 1));
        assertTrue(p.messages.stream().anyMatch(m -> m.contains("Built a Road")));
    }

    @Disabled
    @Test
    void testApplyEffectSettlementWithoutRoad() {
        MockPlayer p = new MockPlayer();
        assertFalse(settlement.applyEffect(p, null, 2, 1));
       // assertTrue(p.messages.stream().anyMatch(m -> m.contains("Settlement must be placed")));
    }

    @Test
    void testApplyEffectDefault() {
        Card other = new Card("Weird", "Basic", "Other", null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
        MockPlayer p = new MockPlayer();
        assertTrue(other.applyEffect(p, null, 1, 1));
        assertEquals(other, p.getCard(1, 1));
    }

    @Disabled
    @Test
    void testMerchantCaravan() {
        Card mc = new Card("Merchant Caravan", "Basic", "Action", null, "Action", null,
                null,null,null,null,null,null,null,null,null,null,null);
        MockPlayer p = new MockPlayer();
        p.setResponses("Grain","Grain","Gold","Gold");
        assertTrue(mc.applyEffect(p,null,0,0));
        assertTrue(p.messages.stream().anyMatch(m -> m.contains("PROMPT: Type Gain")));
    }
    @Disabled
    @Test
    void testGoldsmithNotEnoughGold() {
        Card goldsmith = new Card("Goldsmith", "Basic", "Action", null, "Action", null,
                null,null,null,null,null,null,null,null,null,null,null);
        MockPlayer p = new MockPlayer();
        p.removeResourceSuccess = false;
        assertFalse(goldsmith.applyEffect(p,null,0,0));
        assertTrue(p.messages.stream().anyMatch(m -> m.contains("you need 3 Gold")));
    }

    @Disabled
    @Test
    void testGoldsmithSuccess() {
        Card goldsmith = new Card("Goldsmith", "Basic", "Action", null, "Action", null,
                null,null,null,null,null,null,null,null,null,null,null);
        MockPlayer p = new MockPlayer();
        p.setResponses("Brick","Grain");
        assertTrue(goldsmith.applyEffect(p,null,0,0));
        assertTrue(p.messages.stream().anyMatch(m -> m.contains("Pick resource")));
    }

    @Test
    void testScoutFlagSet() {
        Card scout = new Card("Scout", "Basic", "Action", null, "Action", null,
                null,null,null,null,null,null,null,null,null,null,null);
        MockPlayer p = new MockPlayer();
        assertTrue(scout.applyEffect(p,null,0,0));
        assertTrue(p.flags.contains("SCOUT_NEXT_SETTLEMENT"));
    }

    @Test
    void testBrigittaFlagSet() {
        Card b = new Card("Brigitta the Wise Woman", "Basic", "Action", null, "Action", null,
                null,null,null,null,null,null,null,null,null,null,null);
        MockPlayer p = new MockPlayer();
        assertTrue(b.applyEffect(p,null,0,0));
        assertTrue(p.flags.contains("BRIGITTA"));
    }

    @Disabled
    @Test
    void testRelocationBadChoice() {
        Card r = new Card("Relocation", "Basic", "Action", null, "Action", null,
                null,null,null,null,null,null,null,null,null,null,null);
        MockPlayer p = new MockPlayer();
        p.setResponses("X"); // neither REGION nor EXP
        assertFalse(r.applyEffect(p,null,0,0));
        assertTrue(p.messages.stream().anyMatch(m -> m.contains("canceled")));
    }

    @Disabled
    @Test
    void testRelocationRegionSwap() {
        Card r = new Card("Relocation", "Basic", "Action", null, "Action", null,
                null,null,null,null,null,null,null,null,null,null,null);
        MockPlayer p = new MockPlayer();
        Card reg1 = new Card("Field","Basic","Region",null,null,null,null,null,null,null,null,null,null,null,null,null,null);
        Card reg2 = new Card("Forest","Basic","Region",null,null,null,null,null,null,null,null,null,null,null,null,null,null);
        p.placeCard(1,1,reg1);
        p.placeCard(3,1,reg2);
        p.setResponses("REGION","1 1","3 1");
        assertTrue(r.applyEffect(p,null,0,0));
        assertTrue(p.messages.stream().anyMatch(m -> m.contains("Regions swapped")));
    }

    @Test
    void testRelocationExpansionSwapFail() {
        Card r = new Card("Relocation", "Basic", "Action", null, "Action", null,
                null,null,null,null,null,null,null,null,null,null,null);
        MockPlayer p = new MockPlayer();
        p.setResponses("EXP","1 1","2 2");
        assertFalse(r.applyEffect(p,null,0,0));
    }

    @Disabled
    @Test
    void testDefaultActionCard() {
        Card x = new Card("UnknownAction","Basic","Action",null,"Action",null,
                null,null,null,null,null,null,null,null,null,null,null);
        MockPlayer p = new MockPlayer();
        int before = p.victoryPoints;
        assertTrue(x.applyEffect(p,null,0,0));
        assertEquals(before+1,p.victoryPoints);
        assertTrue(p.messages.stream().anyMatch(m -> m.contains("+1 VP")));
    }

    // --- MockPlayer implementation ---
    static class MockPlayer extends Player {
        Card[][] grid = new Card[5][5];
        Queue<String> responses = new ArrayDeque<>();
        List<String> messages = new ArrayList<>();
        boolean removeResourceSuccess = true;

        void setResponses(String...vals) {
            responses.clear();
            responses.addAll(Arrays.asList(vals));
        }

        @Override public String receiveMessage() { return responses.isEmpty()? "" : responses.poll(); }
        @Override public Card getCard(int r,int c) {
            if (r<0||r>=grid.length||c<0||c>=grid[0].length) return null;
            return grid[r][c];
        }
        @Override public void placeCard(int r,int c,Card card) { grid[r][c]=card; }
        @Override public int expandAfterEdgeBuild(int c){ return c; }
        @Override public boolean hasInPrincipality(String nm){ return false; }
        @Override public int totalAllResources(){ return 3; }
        @Override public boolean removeResource(String nm,int n){ return removeResourceSuccess; }
        @Override public void gainResource(String nm) {}
    }
}
