package qm.game.card;

import java.util.Objects;

public class Card
{
  public final Suit suit;
  public final Rank rank;

  public Card(Suit suit,
              Rank rank)
  {
    this.suit = suit;
    this.rank = rank;
  }

  @Override
  public String toString()
  {
    return "Card{" +
        "suit=" + suit +
        ", rank=" + rank +
        '}';
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (!(o instanceof Card)) return false;
    Card card = (Card) o;
    return suit == card.suit &&
        rank == card.rank;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(suit, rank);
  }
}
