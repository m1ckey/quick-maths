package qm.game.player;

import qm.game.card.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BigShaq
    implements Player
{
  private static final AtomicInteger SEQUENCE = new AtomicInteger();
  private final int serial = SEQUENCE.getAndIncrement();

  private List<Card> cards = new ArrayList<>();
  private List<Card> dealersCards = new ArrayList<>();

  @Override
  public String getName()
  {
    return this.getClass().getSimpleName() + "-" + serial;
  }

  @Override
  public void notifyShuffle()
  {
  }

  @Override
  public void handleResult(Result result,
                           int payout)
  {

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
    return 0;
  }

  @Override
  public void receiveCard(Card c,
                          Player p)
  {
    if (p == null) {
      dealersCards.add(c);
    }
    if (p == this) {
      cards.add(c);
    }
  }
}
