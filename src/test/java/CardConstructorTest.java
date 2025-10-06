import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CardConstructorTest {

    @Test
    void testDefaultConstructor() {
        Card c = new Card();
        assertNotNull(c);
        assertNull(c.name);
    }

    @Test
    void testFullConstructorAndToStringCompareTo() {
        Card c1 = new Card("A", "theme", "type", "gName", "placement",
                "oneOf", "cost", "1", "1","2","3","4","5","6","Req","text","prot");
        Card c2 = new Card("B", "theme", "type", "gName", "placement",
                "oneOf", "cost", "1", "1","2","3","4","5","6","Req","text","prot");
        assertEquals("A", c1.toString());
        assertTrue(c1.compareTo(c2) < 0);
    }
}
