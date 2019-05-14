package qm.game.card;

import java.util.List;

import static qm.game.GameConfig.BLACKJACK;
import static qm.game.card.Rank.ACE;
import static qm.game.card.Rank.ACE_ALT_VALUE;

public class Cards
{
  public static int countMin(List<Card> cards)
  {
    return countMax(cards) - countAces(cards) * (ACE.value - ACE_ALT_VALUE);
  }

  public static int countAces(List<Card> cards)
  {
    return (int) cards.stream().filter(c -> c.rank == ACE).count();
  }

  public static int countMax(List<Card> cards)
  {
    return cards.stream()
        .mapToInt(c -> c.rank.value)
        .sum();
  }

  public static int countIdeal(List<Card> cards)
  {
    int aces = countAces(cards);
    int count = countMax(cards) - aces * ACE.value;
    for (int i = 0; i < aces; i++) {
      if (count + ACE.value > BLACKJACK) {
        count += ACE_ALT_VALUE;
      } else {
        count += ACE.value;
      }
    }

    return count;
  }

  public static boolean isSoft(List<Card> cards)
  {
    return cards.stream().anyMatch(c -> c.rank == ACE);
  }
}
