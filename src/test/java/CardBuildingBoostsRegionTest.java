import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CardBuildingBoostsRegionTest {

    @Test
    void testMatchingBoosts() {
        assertTrue(Card.buildingBoostsRegion("Iron Foundry","Mountain"));
        assertTrue(Card.buildingBoostsRegion("Grain Mill","Field"));
        assertTrue(Card.buildingBoostsRegion("Weaver's Shop","Pasture"));
    }

    @Test
    void testNonMatchingBoosts() {
        assertFalse(Card.buildingBoostsRegion("Iron Foundry","Field"));
        assertFalse(Card.buildingBoostsRegion(null,"Mountain"));
        assertFalse(Card.buildingBoostsRegion("Lumber Camp",null));
    }
}
