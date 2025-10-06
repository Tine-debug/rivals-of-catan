import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class CardHelpersTest {

    @Test
    void testNmEquals() {
        assertTrue(Card.nmEquals("a","A"));
        assertFalse(Card.nmEquals(null,"A"));
    }

    @Test
    void testNmAt() {
        Card c = new Card(); c.name="X";
        assertTrue(Card.nmAt(c,"x","y"));
        assertFalse(Card.nmAt(c,"a","b"));
        assertFalse(Card.nmAt(null,"x","y"));
    }

    @Test
    void testAsInt() {
        assertEquals(123, Card.asInt("123",0));
        assertEquals(0, Card.asInt(null,0));
        assertEquals(42, Card.asInt("invalid",42));
    }

    @Test
    void testGsGi() {
        JsonObject o = JsonParser.parseString("{\"a\":10,\"b\":\"x\"}").getAsJsonObject();
        assertEquals("x", Card.gs(o,"b"));
        assertNull(Card.gs(o,"missing"));
        assertEquals(10, Card.gi(o,"a",0));
        assertEquals(5, Card.gi(o,"missing",5));
    }
}
