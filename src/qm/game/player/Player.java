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
  protected List<List<Card>> cards = new ArrayList<>();
  protected List<Card> dealersCards = new ArrayList<>();

  protected double balance = 0;

  protected boolean firstMove = true;
  protected int split = 0;

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
  }

  public void handleNewRound()
  {
    firstMove = true;
    cards.clear();
    cards.add(new ArrayList<>());
    split = 0;
    dealersCards.clear();
  }

  protected Card getDealersCard()
  {
    if (dealersCards.size() != 1) {
      throw new RuntimeException("dealer should have one card");
    }
    return dealersCards.get(0);
  }

  public void handleShuffle()
  {
  }

  public void handleDraw(Card c,
                         Player p)
  {
    if (p == this) {
      cards.get(split).add(c);
    }
  }

  public void handleDealerDraw(Card c) {
    dealersCards.add(c);
  }

  public abstract Move play();

  public abstract int placeBet();

  public double getBalance()
  {
    return balance;
  }

  @Override
  public String toString()
  {
    return String.format("%s-%03d", this.getClass().getSimpleName(), serial);
  }
}
