import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardApplyEffectTest {

  static class StubPlayer extends Player {
    public Card[][] board = new Card[5][5];
    public int numres = 10;

    @Override
    public void placeCard(int r, int c, Card card) { board[r][c] = card; }

    @Override
    public Card getCard(int r, int c) { 
        return (r<0||r>=5||c<0||c>=5)?null:board[r][c]; 
    }

    @Override
    public int expandAfterEdgeBuild(int col) { return col; }

    @Override
    public boolean hasInPrincipality(String nm) { 
    for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 5; j++) {
            if (board[i][j] != null && board[i][j].name.equalsIgnoreCase(nm)) return true;
        }
    }
    return false;
    }


    @Override
    public void sendMessage(Object msg) {}

    @Override
    public String receiveMessage() { return "Brick"; }

    @Override
    public int totalAllResources() { return this.numres; }

    @Override
    public boolean removeResource(String r,int amt) { return true; }

    @Override
    public void gainResource(String r) {}
}

    StubPlayer p;

    @BeforeEach
    void setup(){ p = new StubPlayer(); }

    @Test
    void testApplyRoad() {
        Card c = new Card(); c.name="Road"; c.type=""; c.placement="";
        assertTrue(c.applyEffect(p,p,2,2));
        assertEquals(c, p.getCard(2,2));
    }

    @Test
    void testApplySettlementNextToRoad() {
        Card road = new Card(); road.name="Road";
        p.placeCard(2,1,road);
        Card sett = new Card(); sett.name="Settlement";
        assertTrue(sett.applyEffect(p,p,2,2));
    }


    @Test
    void testApplySettlementwithoutRoad(){
        Card sett = new Card(); sett.name="Settlement";
        assertFalse(sett.applyEffect(p,p,2,2));
    }

    @Test
    void testApplyCityOnSettlement() {
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card city = new Card(); city.name="City";
        assertTrue(city.applyEffect(p,p,2,2));
        assertEquals(city,p.getCard(2,2));
    }

    @Test
    void testApplyRegion() {
        Card reg = new Card(); reg.name="R"; reg.type="Region"; reg.regionProduction=0;
        assertFalse(reg.applyEffect(p,p,0,0));
        assertTrue(reg.applyEffect(p, p, 1, 1));
        assertEquals(1, reg.regionProduction);
    }

    @Test
    void testApplyUnitHero() {
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card hero = new Card(); hero.name="Hero"; hero.type="Unit"; hero.SP="2"; hero.FP="1"; hero.placement="Settlement/city";
        assertTrue(hero.applyEffect(p,p,1,2));
        assertEquals(1,p.skillPoints);
        assertEquals(2,p.strengthPoints);
    }

    @Test
    void testApplyActionMerchantCaravan() {
        Card m = new Card(); m.name="Merchant Caravan"; m.placement="Action";
        assertTrue(m.applyEffect(p,p,0,0));
        p.numres = 1;
        assertFalse(m.applyEffect(p,p,0,0));
    }

    @Test
    void testApplyActionScout() {
        Card scout = new Card(); scout.name="Scout"; scout.placement="Action"; scout.type="Action";
        assertTrue(scout.applyEffect(p,p,0,0));
        assertTrue(p.flags.contains("SCOUT_NEXT_SETTLEMENT"));
        
    }

    @Test
    void testApplyEffectFallback() {
        Card c = new Card(); c.name="X"; c.type="Other"; c.placement="Other";
        assertTrue(c.applyEffect(p,p,1,1));
        assertEquals(c, p.getCard(1,1));
    }

    @Test
    void testcityillegalplacement(){
        Card city = new Card(); city.name="City";
        assertFalse(city.applyEffect(p,p,2,2));
        Card sett = new Card(); sett.name="Road";
        p.placeCard(2,2,sett);
        assertFalse(city.applyEffect(p,p,2,2));
    }

    @Test
    void testcardonoccupiedslot(){
        Card sett = new Card(); sett.name="rand";
        p.placeCard(2,2,sett);
        Card rand = new Card(); rand.name="rand";
        assertFalse(rand.applyEffect(p,p,2,2));
    }

    @Test
    void testplaceRaodnotCenter (){
        Card road = new Card(); road.name = "Road";
        assertFalse(road.applyEffect(p, p, 1, 1));

    }

    @Test
    void testNotaboveSettlement(){
        Card rand = new Card(); rand.placement = "Settlement/city";
        assertFalse(rand.applyEffect(p, p, 1, 1));

    }

    @Test
    void testoneof(){
        Card rand = new Card(); rand.placement = "Settlement/city"; rand.name = "rand"; rand.type= "Unit";
        rand.oneOf = "1x";
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        assertTrue(rand.applyEffect(p, p, 1, 2));
        assertFalse(rand.applyEffect(p, p, 3, 2));
    }

    @Test
    void testplaybuilding(){
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card building = new Card(); building.name = "Abbey"; building.placement = "Settlement/city"; building.type = "Building";
        assertTrue(building.applyEffect(p, p, 1, 2));
        assertEquals(p.progressPoints, 1);
    }

    @Test
    void testplayMarketplace(){
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card building = new Card(); building.name = "Marketplace"; building.placement = "Settlement/city"; building.type = "Building";
        assertTrue(building.applyEffect(p, p, 1, 2));
        assertTrue(p.flags.contains("MARKETPLACE"));
    }

    @Test
    void testplayParishHall(){
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card building = new Card(); building.name = "Parish Hall"; building.placement = "Settlement/city"; building.type = "Building";
        assertTrue(building.applyEffect(p, p, 1, 2));
        assertTrue(p.flags.contains("PARISH"));
    }

    @Test
    void testplayStorehouse(){
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card building = new Card(); building.name =  "Storehouse"; building.placement = "Settlement/city"; building.type = "Building";
        assertTrue(building.applyEffect(p, p, 1, 2));
        assertTrue(p.flags.contains("STOREHOUSE@1,2"));
    }

     @Test
    void testplayTollBridge(){
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card building = new Card(); building.name =  "Toll Bridge"; building.placement = "Settlement/city"; building.type = "Building";
        assertTrue(building.applyEffect(p, p, 1, 2));
        assertTrue(p.flags.contains("TOLLB"));
    }

    @Test
    void testplayRandbuild(){
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card building = new Card(); building.name =  "Rand"; building.placement = "Settlement/city"; building.type = "Building";
        assertTrue(building.applyEffect(p, p, 1, 2));
    }

    @Test
    void testplayLargeTradeShip(){
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card unit = new Card(); unit.name =  "Large Trade Ship"; unit.placement = "Settlement/city"; unit.type = "Unit";
        assertTrue(unit.applyEffect(p, p, 1, 2));
        assertTrue(p.flags.contains("LTS@1,2"));
    }

    @Test
    void testplayBrickShip(){
        Card sett = new Card(); sett.name="Settlement";
        p.placeCard(2,2,sett);
        Card unit = new Card(); unit.name =  "Brick ship"; unit.placement = "Settlement/city"; unit.type = "Unit";
        assertTrue(unit.applyEffect(p, p, 1, 2));
        System.out.println(p.flags.toString());
        assertTrue(p.flags.contains("2FOR1_BRICK"));
    }

}
