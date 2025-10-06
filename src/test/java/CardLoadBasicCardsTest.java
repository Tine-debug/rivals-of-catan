import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CardLoadBasicCardsTest {

    @Test
    void testLoadBasicCards() throws IOException {
        // Prepare temporary JSON file
        File temp = File.createTempFile("cards", ".json");
        temp.deleteOnExit();
        String json = "[\n" +
                "  {\"name\":\"Road\",\"theme\":\"Basic\",\"type\":\"\",\"placement\":\"\",\"number\":2},\n" +
                "  {\"name\":\"Settlement\",\"theme\":\"Basic\",\"type\":\"\",\"placement\":\"\",\"number\":1},\n" +
                "  {\"name\":\"City\",\"theme\":\"Basic\",\"type\":\"\",\"placement\":\"\",\"number\":1},\n" +
                "  {\"name\":\"Mountain\",\"theme\":\"Basic\",\"type\":\"Region\",\"placement\":\"\",\"number\":1},\n" +
                "  {\"name\":\"EventX\",\"theme\":\"Basic\",\"type\":\"\",\"placement\":\"Event\",\"number\":1},\n" +
                "  {\"name\":\"Extra\",\"theme\":\"Basic\",\"type\":\"Expansion\",\"placement\":\"Settlement/city\",\"number\":5}\n" +
                "]";
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write(json);
        }

        // Clear static piles first
        Card.regions.clear();
        Card.roads.clear();
        Card.settlements.clear();
        Card.cities.clear();
        Card.events.clear();
        Card.drawStack1.clear();
        Card.drawStack2.clear();
        Card.drawStack3.clear();
        Card.drawStack4.clear();

        // Load
        Card.loadBasicCards(temp.getAbsolutePath());

        // Assertions
        assertEquals(2, Card.roads.size());
        assertEquals(1, Card.settlements.size());
        assertEquals(1, Card.cities.size());
        assertEquals(1, Card.regions.size());
        assertEquals(1, Card.events.size());
        // Remaining cards go to draw stacks
        int totalDraw = Card.drawStack1.size() + Card.drawStack2.size() +
                        Card.drawStack3.size() + Card.drawStack4.size();
        assertEquals(5 - 0, totalDraw); // Extra=5 minus any extracted cards
    }
}
