import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CardPopCardByNameTest {

    @Test
    void testPopCardByNameFound() {
        Vector<Card> v = new Vector<>();
        Card c = new Card(); c.name="Test";
        v.add(c);
        Card popped = Card.popCardByName(v, "Test");
        assertSame(c, popped);
        assertTrue(v.isEmpty());
    }

    @Test
    void testPopCardByNameNotFound() {
        Vector<Card> v = new Vector<>();
        assertNull(Card.popCardByName(v, "Missing"));
    }

    @Test
    void testPopCardByNameNulls() {
        assertNull(Card.popCardByName(null, "X"));
        Vector<Card> v = new Vector<>();
        v.add(null);
        assertNull(Card.popCardByName(v, null));
    }
}
