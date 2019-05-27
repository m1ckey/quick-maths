package qm;

import qm.game.Game;
import qm.game.player.Player;
import qm.game.player.SimpleStacy;

public class Launcher
{
  public static void main(String[] args) throws InterruptedException
  {
    Game g = buildGame();

    Thread t = new Thread(g);
    t.start();
    t.join(30_000);
    t.interrupt();

    System.out.printf("played %d rounds%n", g.getRound());
    g.getPlayers().forEach(p -> System.out.printf("%16s: %10.2f%n", p, p.getBalance()));
  }

  private static Game buildGame() {
    Game g = new Game();
    for (int i = 0; i < g.getConfig().getMaxPlayers(); i++) {
      Player p = new SimpleStacy(g);
      g.join(p);
    }
    return g;
  }
}
