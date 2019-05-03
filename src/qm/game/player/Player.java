package qm.game.player;

import qm.game.GameState;

public interface Player
{
  String getName();

  Move play(GameState state);
}
