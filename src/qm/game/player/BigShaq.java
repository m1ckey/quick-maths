package qm.game.player;

import qm.game.GameState;

import java.util.concurrent.atomic.AtomicInteger;

public class BigShaq
    implements Player
{
  private static final AtomicInteger SEQUENCE = new AtomicInteger();
  private final int serial = SEQUENCE.getAndIncrement();

  @Override
  public String getName()
  {
    return this.getClass().getSimpleName() + "-" + serial;
  }

  @Override
  public Move play(GameState state)
  {
    // i guess they never miss huh
    return Move.HIT;
  }
}
