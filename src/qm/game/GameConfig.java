package qm.game;

import qm.game.card.Card;
import qm.game.card.Rank;
import qm.game.card.Suit;

import java.util.ArrayList;
import java.util.List;

public class GameConfig
{
  private int maxPlayers = 6;
  private int decks = 6;

  private List<Card> deck;

  {
    List<Card> l = new ArrayList<>(52);
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
    return deck;
  }

  public void setDeck(List<Card> deck)
  {
    this.deck = List.copyOf(deck);
  }
}
