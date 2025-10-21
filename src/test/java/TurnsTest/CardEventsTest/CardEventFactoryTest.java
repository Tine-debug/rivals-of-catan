package TurnsTest.CardEventsTest;

import Utils.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import java.io.IOException;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Card.*;
import Card.Cardstack.CardstackFacade;
import Card.logic.*;
import Player.*;
import Turns.*;
import Turns.Cardevents.*;
import Turns.Phases.*;
import Points.*;

public class CardEventFactoryTest {

    @Test
    public void cerateFeudTest() {
        CardEvent event = CardEventFactory.createCardEvent("feud");
        assertTrue(event instanceof feudEvent);
    }

    @Test
    public void ceratefraternalFeudTest() {
        CardEvent event = CardEventFactory.createCardEvent("fraternal feuds");
        assertTrue(event instanceof fraternalfeudsEvent);
    }

    @Test
    public void cerateInventionTest() {
        CardEvent event = CardEventFactory.createCardEvent("invention");
        assertTrue(event instanceof inventionEvent);
    }

    @Test
    public void cerateTradeShipRaceTest() {
        CardEvent event = CardEventFactory.createCardEvent("trade ships race");
        assertTrue(event instanceof tradeShipRaceEvent);
    }

    @Test
    public void cerateMerchantTest() {
        CardEvent event = CardEventFactory.createCardEvent("traveling merchant");
        assertTrue(event instanceof travelingMerchantEvent);
    }

    @Test
    public void cerateYearOfPlentyTest() {
        CardEvent event = CardEventFactory.createCardEvent("year of plenty");
        assertTrue(event instanceof yearOfThePlentyEvent);
    }

    @Test
    public void cerateYuleTest() {
        CardEvent event = CardEventFactory.createCardEvent("yule");
        assertTrue(event instanceof yuleEvent);
    }

    @Test
    public void createDefaultTest(){
        CardEvent event = CardEventFactory.createCardEvent("Samalamadingdong");
        assertTrue(event instanceof  defaultEvent);
    }

}
