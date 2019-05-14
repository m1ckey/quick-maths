package qm.game.card;

public enum Rank
{
  ACE(11), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10);

  public static final int ACE_ALT_VALUE = 1;

  public final int value;

  Rank(int value)
  {
    this.value = value;
  }
}
