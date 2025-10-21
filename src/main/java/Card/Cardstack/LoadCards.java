package Card.Cardstack;

import Card.Card;
import Card.Cardbuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import Points.PointsBuilder;
import Card.logic.LogicFactory;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LoadCards {

    public static ArrayList<Card> loadCards(String jsonPath, boolean loadmultiple, String desiredTheme) throws IOException {

        ArrayList<Card> allBasic = new ArrayList<>();

        try (FileReader fr = new FileReader(jsonPath)) {
            JsonElement root = JsonParser.parseReader(fr);
            if (!root.isJsonArray()) {
                throw new IOException("cards.json: expected top-level array");
            }
            JsonArray arr = root.getAsJsonArray();

            for (JsonElement el : arr) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject o = el.getAsJsonObject();
                String theme = gs(o, "theme");
                if (theme == null || !theme.toLowerCase().contains(desiredTheme.toLowerCase())) {
                    continue;
                }
                int number;
                if (loadmultiple) {
                    number = gi(o, "number", 1);
                } else {
                    number = 1;
                }
                for (int i = 0; i < number; i++) {
                    Cardbuilder cardbuilder = new Cardbuilder();
                    String name = gs(o, "name");
                    cardbuilder.name(name);
                    String type = gs(o, "type");
                    cardbuilder.type(type);
                    String placement = gs(o, "placement");
                    cardbuilder.placement(placement);
                    cardbuilder.logic(LogicFactory.createLogic(placement, name, type));

                    cardbuilder.oneOf(gs(o, "oneOf"));
                    cardbuilder.cardText(gs(o, "cardText"));
                    cardbuilder.cost(gs(o, "cost"));

                    PointsBuilder pointsBuilder = new PointsBuilder();
                    pointsBuilder.victoryPoints(gs(o, "victoryPoints"));
                    pointsBuilder.commercePoints(gs(o, "CP"));
                    pointsBuilder.strengthPoints(gs(o, "SP"));
                    pointsBuilder.skillPoints(gs(o, "FP"));
                    pointsBuilder.progressPoints(gs(o, "PP"));
                    pointsBuilder.sailPoints(gs(o, "LP"));
                    pointsBuilder.canonPoints(gs(o, "KP"));

                    cardbuilder.points(pointsBuilder.build());

                    allBasic.add(cardbuilder.build());
                }
            }
        }

        return allBasic;

    }

    private static String gs(JsonObject o, String k) {
        if (!o.has(k)) {
            return null;
        }
        JsonElement e = o.get(k);
        return (e == null || e.isJsonNull()) ? null : e.getAsString();
    }

    private static int gi(JsonObject o, String k, int def) {
        if (!o.has(k)) {
            return def;
        }
        try {
            return o.get(k).getAsInt();
        } catch (Exception e) {
            return def;
        }
    }
}
