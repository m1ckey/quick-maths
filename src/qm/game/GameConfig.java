package qm.game;

import qm.game.card.Card;
import qm.game.card.Rank;
import qm.game.card.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameConfig
{
  public static final int BLACKJACK = 21;
  public static final int DEALER_THRESHOLD = 17;

  private int maxPlayers = 6;
  private int betLimit = 400;

  private int decks = 6;
  private List<Card> deck;
  private boolean dealerStandsOnSoftThreshold = true;

  {
    List<Card> l = new ArrayList<>(Suit.values().length * Rank.values().length);
    for (Suit s : Suit.values()) {
      for (Rank r : Rank.values()) {
        l.add(new Card(s, r));
      }
    }
    setDeck(l);
  }

  public GameConfig()
  {

  }

  public GameConfig(GameConfig config)
  {
    setMaxPlayers(config.maxPlayers);
    setDecks(config.decks);
    setDeck(config.deck);
    setBetLimit(config.betLimit);
    setDealerStandsOnSoftThreshold(config.dealerStandsOnSoftThreshold);
  }

  public int getMaxPlayers()
  {
    return maxPlayers;
  }

  public void setMaxPlayers(int maxPlayers)
  {
    this.maxPlayers = maxPlayers;
  }

  public int getDecks()
  {
    return decks;
  }

  public void setDecks(int decks)
  {
    this.decks = decks;
  }

  public List<Card> getDeck()
  {
    return Collections.unmodifiableList(deck);
  }

  public void setDeck(List<Card> deck)
  {
    this.deck = List.copyOf(deck);
  }

  public int getBetLimit()
  {
    return betLimit;
  }

  public void setBetLimit(int betLimit)
  {
    this.betLimit = betLimit;
  }

  public boolean getDealerStandsOnSoftThreshold()
  {
    return dealerStandsOnSoftThreshold;
  }

  public void setDealerStandsOnSoftThreshold(boolean dealerStandsOnSoftThreshold)
  {
    this.dealerStandsOnSoftThreshold = dealerStandsOnSoftThreshold;
  }
}
