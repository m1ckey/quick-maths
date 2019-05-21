package qm.game.player;

import qm.game.Game;

public class BigShaq
    extends Player
{
  public BigShaq(Game game)
  {
    super(game);
  }

  @Override
  public Move play()
  {
    // i guess they never miss huh
    return Move.HIT;
  }

  @Override
  public int placeBet()
  {
    balance -= 0;
    return 0;
  }
}
