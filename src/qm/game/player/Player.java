package qm.game.player;

import qm.game.Game;
import qm.game.GameConfig;
import qm.game.card.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Player
{
  private static final AtomicInteger SEQUENCE = new AtomicInteger();
  public final Game game;
  protected final GameConfig config;
  private final int serial = SEQUENCE.getAndIncrement();
  protected List<Card> cards = new ArrayList<>();
  protected List<Card> dealersCards = new ArrayList<>();

  protected double balance = 0;

  public Player(Game game)
  {
    if (game == null) throw new IllegalArgumentException("game cannot be null");
    this.game = game;
    config = game.getConfig();
  }

  public void handleResult(Result result,
                           double payout)
  {
    balance += payout;
    reset();
  }

  protected void reset()
  {
    cards.clear();
    dealersCards.clear();
  }

  protected Card getDealersCard()
  {
    if (dealersCards.size() != 1) {
      throw new RuntimeException("dealer should only have one card");
    }
    return dealersCards.get(0);
  }

  public void handleShuffle()
  {
  }

  public void handleCard(Card c,
                         Player p)
  {
    if (p == null) {
      dealersCards.add(c);
    }
    if (p == this) {
      cards.add(c);
    }
  }

  public abstract Move play();

  public abstract int placeBet();

  @Override
  public String toString()
  {
    return String.format("%s-%03d", this.getClass().getSimpleName(), serial);
  }
}
