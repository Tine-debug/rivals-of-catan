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


public class resolveOneTurnTest{

Cardstacks stacks = Cardstacks.getInstance();

    Server server = new MockServer();

    @BeforeEach
    public void setup(){
        try {
                stacks.loadBasicCardsoptionalshuffle(false, "cards.json");
        } catch (Exception e) {
        }
    }




}