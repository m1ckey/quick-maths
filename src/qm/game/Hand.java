package qm.game;

import qm.game.card.Card;
import qm.game.exception.IllegalMoveException;
import qm.game.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand
{
  public final Player player;
  private final List<Card> cards = new ArrayList<>();

  public Hand(Player player)
  {
    this.player = player;
  }

  public List<Card> getCards()
  {
    return Collections.unmodifiableList(cards);
  }

  public void add(Card c) {
    cards.add(c);
  }

  public Hand split() {
    if(!(cards.size() == 2 && cards.get(0).rank.equals(cards.get(1).rank))) {
      throw new IllegalMoveException("cannot split");
    }

    Hand h = new Hand(player);
    h.add(cards.get(1));
    cards.remove(1);
    return h;
  }
}
