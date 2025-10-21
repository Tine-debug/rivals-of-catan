package Card.Cardstack;

import Player.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Card.Card;

public class CardstackFacade {

    private final RegionManager regionManager;
    private final CenterBuildingManager buildingManager;
    private final EventManager eventManager;
    private final DrawStackManager drawStackManager;
    private final CardUtils cardUtils;

    private CardstackFacade() {
        this.regionManager = new RegionManager();
        this.buildingManager = new CenterBuildingManager();
        this.eventManager = new EventManager();
        this.drawStackManager = new DrawStackManager();
        this.cardUtils = new CardUtils();
    }

    static private CardstackFacade instance = null;

    static public CardstackFacade getInstance() {
        if (instance == null) {
            instance = new CardstackFacade();
        }
        return instance;
    }

    public void loadCards(String jsonPath, String theme) throws IOException{
        List<Card> all = LoadCards.loadCards(jsonPath, true, theme);
        int allsize = all.size();

        buildingManager.initializeCenterCards(all);
        regionManager.initializeRegions(all);
        eventManager.initializeEvents(all, true);
        drawStackManager.initializeStacks(all, true, allsize);

    }

    public List<Card> getStack(String Name) {
        return this.buildingManager.getStack(Name);
    }

    public List<Card> loadCardsForTesting(String jsonPath, String desiredTheme, boolean shuffle, boolean loadmulitpe) throws IOException {
        List<Card> all = LoadCards.loadCards(jsonPath, loadmulitpe, "basic");
        List<Card> out = new ArrayList<>(all);
        buildingManager.initializeCenterCards(all);
        regionManager.initializeRegions(all);
        eventManager.initializeEvents(all, shuffle);
        drawStackManager.initializeStacks(all, shuffle, out.size());
        return out;
    }

    public void initializePlayerPrincipality(Player p, int[][] dice, int center, int idx) {
        this.buildingManager.inizializePrincipiality(p, dice, center, idx, regionManager.getRegions());
    }

    public void shuffleRegions() {
        regionManager.shuffle();
    }

    public Card drawRegionCard() {
        return regionManager.draw();
    }

    public int getRegionStackSize() {
        return regionManager.size();
    }

    public String printRegionStack() {
        return regionManager.print();
    }

    public void resetEventStack() {
        eventManager.reset();
    }

    public Card drawEventCard() {
        return eventManager.draw();
    }

    public void placeCardBottomStack(Card c, int stackNum) {
        drawStackManager.placeAtBottom(c, stackNum);
    }

    public void drawCardFromStack(int stack, Player p) {
        drawStackManager.drawCard(stack, p);
    }

    public void chooseCardFromStack(int stack, Player p) {
        drawStackManager.chooseCard(stack, p);
    }

    public void drawCard(int stack, Player p) {
        drawStackManager.drawCard(stack, p);
    }

    public int[] placeCenterCard(Player active, Player other, String cardType) {
        return buildingManager.placeCenterCard(active, other, cardType);
    }

    public List<String> getCenterBuildingCosts() {
        return buildingManager.getBuildingCosts();
    }

    public Card pickRegionByNameOrIndex(String spec) {
        return regionManager.pickByNameOrIndex(spec);
    }

    public Card findUndicedRegionByName(String name) {
        return regionManager.findUndiced(name);
    }

    public Card popCardByName(List<Card> cards, String name) {
        return CardUtils.popCardByName(cards, name);
    }

}
