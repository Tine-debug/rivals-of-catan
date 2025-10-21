
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import Card.Cardstack.CardstackFacade;

public class loadCardTest {

    public final CardstackFacade stacks = CardstackFacade.getInstance();

    @Test
    public void loadCardtest() {
        try {
            stacks.loadCardsForTesting("cards.json", "basic", false, true);
        } catch (Exception e) {
            System.err.println("Failed to load");
        }
        Approvals.verify(stacks.printRegionStack());

    }
    

}
