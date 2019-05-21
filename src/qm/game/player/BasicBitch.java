package qm.game.player;

import qm.game.Game;

public class BasicBitch
    extends Player
{
  public BasicBitch(Game game)
  {
    super(game);
  }

  @Override
  public Move play()
  {
    // todo: implement basic strategy
    throw new UnsupportedOperationException();
  }

  @Override
  public int placeBet()
  {
    throw new UnsupportedOperationException();
  }
}
