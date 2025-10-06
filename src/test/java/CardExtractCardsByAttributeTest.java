import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CardExtractCardsByAttributeTest {

    @Test
    void testExtractCardsByAttribute() {
        Vector<Card> v = new Vector<>();
        Card c1 = new Card(); c1.name="X";
        Card c2 = new Card(); c2.name="Y";
        v.add(c1); v.add(c2);

        Vector<Card> out = Card.extractCardsByAttribute(v, "name", "Y");
        assertEquals(1, out.size());
        assertEquals("Y", out.get(0).name);
        assertEquals(1, v.size());
        assertEquals("X", v.get(0).name);
    }

    @Test
    void testExtractCardsByAttributeNonexistent() {
        Vector<Card> v = new Vector<>();
        Card c = new Card(); c.name="A"; v.add(c);
        Vector<Card> out = Card.extractCardsByAttribute(v, "name", "B");
        assertTrue(out.isEmpty());
        assertEquals(1, v.size());
    }
}
