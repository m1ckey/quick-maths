package qm.game.player;

import qm.game.Game;
import qm.game.card.Cards;
import qm.game.card.Rank;

import static qm.game.player.Move.*;

// https://wizardofodds.com/games/blackjack/appendix/21/
public class SimpleStacy
    extends Player
{
  private int lastBet;

  public SimpleStacy(Game game)
  {
    super(game);
  }

  @Override
  public Move play()
  {
    Move m = playImpl();
    switch (m) {
      case DOUBLE:
        balance -= lastBet;
        break;
      case SPLIT:
        balance -= lastBet;
        // todo
        break;
    }
    return m;
  }

  private Move playImpl()
  {
    int dealersCount = getDealersCard().rank.value;
    int count = Cards.countIdeal(cards);

    if (cards.get(0).equals(cards.get(1))) {
      switch (count) {
        case 4:
        case 6:
        case 14:
          if (2 <= dealersCount && dealersCount <= 7) return SPLIT;
          break;
        case 8:
          if (5 <= dealersCount && dealersCount <= 6) return SPLIT;
          break;
        case 12: // 6,6 or A,A
          if (cards.get(0).rank == Rank.ACE) return SPLIT;
          if (2 <= dealersCount && dealersCount <= 6) return SPLIT;
          break;
        case 16:
          return SPLIT;
        case 18:
          if ((2 <= dealersCount && dealersCount <= 6) || (8 <= dealersCount && dealersCount <= 9)) return SPLIT;
          break;
        case 10:
        case 20:
          break;
        default:
          throw new RuntimeException("wtf");
      }
    }

    if (!Cards.isSoft(cards)) {
      switch (count) {
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
          return HIT;
        case 9:
          if (3 <= dealersCount && dealersCount <= 6) return DOUBLE;
          return HIT;
        case 10:
        case 11:
          if (count > dealersCount) return DOUBLE;
          return HIT;
        case 12:
        case 13:
        case 14:
        case 15:
        case 16:
          if (count == 12 && (dealersCount == 2 || dealersCount == 3)) return HIT;
          if (dealersCount <= 6) return STAND;
          if (count == 15 && dealersCount == 10) return SURRENDER;
          if (count == 16 && dealersCount >= 9) return SURRENDER;
          return HIT;
        case 17:
        case 18:
        case 19:
        case 20:
        case 21:
          return STAND;
        default:
          throw new RuntimeException("wtf");
      }
    } else {
      switch (count) {
        case 13:
        case 14:
          if (5 <= dealersCount && dealersCount <= 6) return DOUBLE;
          return HIT;
        case 15:
        case 16:
        case 17:
          if (4 <= dealersCount && dealersCount <= 6) return DOUBLE;
          return HIT;
        case 18:
          if (dealersCount == 2 || (7 <= dealersCount && dealersCount <= 8)) return STAND;
          if (3 <= dealersCount && dealersCount <= 6) return DOUBLE;
          return HIT;
        case 19:
        case 20:
        case 21:
          return STAND;
        default:
          throw new RuntimeException("wtf");
      }
    }
  }

  @Override
  public int placeBet()
  {
    int bet = config.getBetLimitMin();
    balance -= bet;
    return lastBet = bet;
  }
}
