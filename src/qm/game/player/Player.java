package qm.game.player;

import qm.game.card.Card;

public interface Player
{
  String getName();

  void handleResult(Result result,
                    int payout);

  void notifyShuffle();

  void receiveCard(Card c,
                   Player p);

  Move play();

  int placeBet();
}
