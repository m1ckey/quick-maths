package qm.game;

import qm.game.card.Card;
import qm.game.card.Cards;
import qm.game.exception.IllegalBetException;
import qm.game.exception.IllegalMoveException;
import qm.game.player.Move;
import qm.game.player.Player;

import java.util.*;

import static qm.game.GameConfig.BLACKJACK;
import static qm.game.player.Result.*;

public class Game
    implements Runnable
{
  private final GameConfig config;
  private final Player[] players;
  private long round;
  private List<Card> cards;
  private int drawIndex = 0;
  private int revenue = 0;

  public Game()
  {
    this(new GameConfig());
  }

  private Map<Player, Integer> bets = new HashMap<>();

  public synchronized int join(Player player)
  {
    for (Player p : players) {
      if (p == player) {
        throw new IllegalArgumentException("player already joined");
      }
    }

    for (int i = 0; i < players.length; i++) {
      if (players[i] == null) {
        players[i] = player;
        return i;
      }
    }

    throw new RuntimeException("max Players exceeded");
  }

  public synchronized void join(Player player,
                                int position)
  {
    if (players[position] != null) {
      throw new IllegalArgumentException("position already taken");
    }
    players[position] = player;
  }

  public synchronized void leave(Player player)
  {
    for (int i = 0; i < players.length; i++) {
      if (players[i] == player) {
        players[i] = null;
      }
    }
  }

  private Map<Player, List<Card>> openCards = new HashMap<>();

  public Game(GameConfig config)
  {
    this.config = new GameConfig(config);

    players = new Player[config.getMaxPlayers()];

    cards = new ArrayList<>(config.getDecks() * config.getDeck().size());
    for (int i = 0; i < config.getDecks(); i++) {
      cards.addAll(config.getDeck());
    }

    shuffleCards();
  }

  @Override
  public synchronized void run()
  {
    // init openCards
    {
      openCards.clear();
      openCards.put(null, new ArrayList<>());
      for (Player p : players) {
        openCards.put(p, new ArrayList<>());
      }
    }

    // get bets
    {
      bets.clear();
      for (Player p : players) {
        int bet = p.placeBet();
        if (bet > config.getBetLimit()) throw new IllegalBetException("bet limit exceeded");
        if (bet <= 0) throw new IllegalBetException("bet cant be <= 0");
        bets.put(p, bet);
      }
    }

    // deal cards
    {
      for (Player p : players) {
        playerDraws(p);
      }
      dealerDraws();
      for (Player p : players) {
        playerDraws(p);
      }
    }

    // play
    {
      for (Player p : players) {
        servePlayer(p);
      }
    }

    // showdown
    {
      int dealerCountIdeal = serveDealer();
      for (Player p : players) {
        int countIdeal = Cards.countIdeal(openCards.get(p));
        if (countIdeal > BLACKJACK) {
          p.handleResult(BUST, 0);
        } else if (countIdeal == dealerCountIdeal) {
          p.handleResult(TIE, bets.get(p));
        } else if (countIdeal < dealerCountIdeal) {
          p.handleResult(LOSS, 0);
        } else {
          p.handleResult(WIN, bets.get(p) * 2);
        }
      }
    }
  }

  private void shuffleCards()
  {
    drawIndex = 0;
    Collections.shuffle(cards);
    for (Player p : players) {
      p.notifyShuffle();
    }
  }

  private Card draw()
  {
    return cards.get(drawIndex++);
  }

  private void servePlayer(Player p)
  {
    while (true) {
      Move m = p.play();
      switch (m) {
        case STAND:
          return;

        case HIT:
          playerDraws(p);
          if (Cards.countMin(openCards.get(p)) > BLACKJACK) {
            return;
          }
          break;

        default:
          throw new IllegalMoveException("unsupported move");
      }
    }
  }

  private int serveDealer()
  {
    while (true) {
      dealerDraws();
      List<Card> dealersCards = openCards.get(null);

      int countIdeal = Cards.countIdeal(dealersCards);
      if (countIdeal > BLACKJACK) {
        return countIdeal;
      }
      if (!config.getDealerStandsOnSoftThreshold() && Cards.isSoft(dealersCards) && countIdeal == GameConfig.DEALER_THRESHOLD) {
        continue;
      }
      if (countIdeal >= GameConfig.DEALER_THRESHOLD) {
        return countIdeal;
      }
    }
  }

  private Card playerDraws(Player p)
  {
    Card c = draw();
    openCards.get(p).add(c);
    p.receiveCard(c, p);
    return c;
  }

  private Card dealerDraws()
  {
    Card c = draw();
    openCards.get(null).add(c);
    for (Player p : players) {
      p.receiveCard(c, null);
    }
    return c;
  }
}
