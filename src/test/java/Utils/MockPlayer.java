package Utils;

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

import Card.*;
import Card.Cardstack.CardstackFacade;
import Card.logic.*;
import Player.*;
import Turns.*;
import Turns.Cardevents.*;
import Turns.Phases.*;
import Points.*;

public class MockPlayer extends Player {

    public List<String> messages = new ArrayList<>();
    public int messagenumber = 0;
    private CardstackFacade stacks = CardstackFacade.getInstance();



    @Override
    public String receiveMessage() {
        if (messages.size() == 0) {
            return "Brick";
        }
        String message = messages.get(messagenumber);
        messagenumber = (messagenumber + 1) % messages.size();
        return message;
    }

}
