package qm.game;

import qm.game.card.Card;
import qm.game.card.Cards;
import qm.game.exception.IllegalBetException;
import qm.game.exception.IllegalMoveException;
import qm.game.player.Player;
import qm.game.player.Result;

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

  public GameConfig getConfig()
  {
    return new GameConfig(config);
  }

  public synchronized int join(Player player)
  {
    for (int i = 0; i < players.length; i++) {
      if (players[i] == null) {
        join(player, i);
        return i;
      }
    }

    throw new RuntimeException("max Players exceeded");
  }

  public synchronized void leave(Player player)
  {
    for (int i = 0; i < players.length; i++) {
      if (players[i] == player) {
        players[i] = null;
      }
    }
  }

  public synchronized void join(Player player,
                                int position)
  {
    if (player == null) throw new IllegalArgumentException("player cannot be null");
    if (player.game != this) throw new IllegalArgumentException("player was not constructed for this game");

    for (Player p : players) {
      if (p == player) {
        throw new IllegalArgumentException("player already joined");
      }
    }

    if (players[position] != null) {
      throw new IllegalArgumentException("position already taken");
    }
    players[position] = player;
  }

  @Override
  public synchronized void run()
  {
    List<Player> players = new ArrayList<>(Arrays.asList(this.players));

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
        if (bet < config.getBetLimitMin()) throw new IllegalBetException("bet limit undershot");
        if (bet > config.getBetLimitMax()) throw new IllegalBetException("bet limit exceeded");
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
      List<Player> disqualifiedPlayers = new ArrayList<>();
      for (Player p : players) {
        boolean disqualified = servePlayer(p);
        if (disqualified) {
          disqualifiedPlayers.add(p);
        }
      }
      players.removeAll(disqualifiedPlayers);
      serveDealer();
    }

    // showdown
    {
      List<Card> dealersCards = openCards.get(null);
      int dealerCountIdeal = Cards.countIdeal(dealersCards);

      for (Player p : players) {
        List<Card> playersCards = openCards.get(p);
        int playerCountIdeal = Cards.countIdeal(playersCards);

        if (Cards.isBlackjack(playersCards) && !Cards.isBlackjack(dealersCards)) {
          p.handleResult(Result.BLACKJACK, bets.get(p) * (1 + config.getBlackjackBonus()));
        } else if (dealerCountIdeal > BLACKJACK) {
          p.handleResult(VICTORY, bets.get(p) * 2);
        } else if (playerCountIdeal == dealerCountIdeal) {
          p.handleResult(TIE, bets.get(p));
        } else if (playerCountIdeal < dealerCountIdeal) {
          p.handleResult(DEFEAT, 0);
        } else {
          p.handleResult(VICTORY, bets.get(p) * 2);
        }
      }
    }
  }

  private void shuffleCards()
  {
    drawIndex = 0;
    Collections.shuffle(cards);
    for (Player p : players) {
      p.handleShuffle();
    }
  }

  private Card draw()
  {
    return cards.get(drawIndex++);
  }

  private boolean servePlayer(Player p)
  {
    boolean canSurrender = true;
    while (true) {
      switch (p.play()) {
        case STAND:
          return false;

        case HIT:
          playerDraws(p);
          if (Cards.countMin(openCards.get(p)) > BLACKJACK) {
            p.handleResult(BUST, 0);
            return true;
          }
          break;

        case SURRENDER:
          if (!canSurrender) throw new IllegalMoveException("player can only surrender on first move");
          p.handleResult(SURRENDER, bets.get(p) * 0.5);
          return true;

        case SPLIT:
          throw new UnsupportedOperationException("move not implemented");

        default:
          throw new RuntimeException("wtf");
      }
      canSurrender = false;
    }
  }

  private void serveDealer()
  {
    while (true) {
      dealerDraws();
      List<Card> dealersCards = openCards.get(null);

      int countIdeal = Cards.countIdeal(dealersCards);
      if (countIdeal > BLACKJACK) {
        return;
      }
      if (!config.getDealerStandsOnSoftThreshold() && Cards.isSoft(dealersCards) && countIdeal == GameConfig.DEALER_THRESHOLD) {
        continue;
      }
      if (countIdeal >= GameConfig.DEALER_THRESHOLD) {
        return;
      }
    }
  }

  private Card playerDraws(Player p)
  {
    Card c = draw();
    openCards.get(p).add(c);
    p.handleCard(c, p);
    return c;
  }

  private Card dealerDraws()
  {
    Card c = draw();
    openCards.get(null).add(c);
    for (Player p : players) {
      p.handleCard(c, null);
    }
    return c;
  }
}
