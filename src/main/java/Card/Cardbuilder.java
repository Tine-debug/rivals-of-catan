package Card;

import Points.Points;
import Card.logic.Logic;

public class Cardbuilder {

    private String name, theme, type, cost;
    private boolean oneOf;
    private String cardText;
    private Points points;
    private String placement;
    private Logic logic;



    public void name(String name) {
        this.name = name;
    }

    public void theme(String theme) {
        this.theme = theme;
    }

    public void type(String type) {
        this.type = type;
    }

    public void cost(String cost) {
        this.cost = cost;
    }

    public void oneOf(boolean oneOf){
        this.oneOf = oneOf;
    }

    public void oneOf(String oneOf){
        this.oneOf = (oneOf == null) ? false : (oneOf.equalsIgnoreCase("1x"));
    }

    public void cardText(String cardText){
        this.cardText = cardText;
    }

    public void points(Points points){
        this.points = points;
    }

    public void placement(String placement){
        this.placement = placement;
    }

    public void logic(Logic logic){
        this.logic = logic;
    }


    public Card build(){
        return new Card(this.name, this.theme, this.type, this.placement, 
        this.oneOf, this.cost, this.points, this.cardText, 
        this.logic);
    }

}
