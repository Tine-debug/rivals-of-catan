public class MockServer extends Server{

    int eventdie = 0;
    int productiondie = 0;

    @Override
    protected int rollEventDie(Player active){
        eventdie = (eventdie + 1)%6;
        return eventdie + 1;
    }

    @Override
    protected int rollProductionDie(Player active) {
        productiondie = (productiondie + 1) % 6;
        int face = 1 + productiondie;
        if (active.flags.contains("BRIGITTA")) {
            active.sendMessage("PROMPT: Brigitta active -  choose production die [1-6]:");
            try {
                int forced = Integer.parseInt(active.receiveMessage().trim());
                if (forced >= 1 && forced <= 6)
                    face = forced;
            } catch (Exception ignored) {
            }
            active.flags.remove("BRIGITTA");
        }
        return face;
    }

}