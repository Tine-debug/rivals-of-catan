
public class inventionEvent implements Event {

    @Override
    public void resolve(Player active, Player other) {
        Player p;
        for (int j = 0; j < 2; j++) {
            p = j == 0 ? active : other;

            int times = Math.min(2, Math.max(0, p.points.progressPoints));
            if (times == 0) {
                p.sendMessage("Invention: you have no progress point buildings (max 2).");
                continue;
            }
            for (int i = 0; i < times; i++) {
                p.sendMessage("PROMPT: Invention - gain resource #" + (i + 1) + " of your choice:");
                String res = p.receiveMessage();
                p.gainResource(res);
            }
        }
    }

}
