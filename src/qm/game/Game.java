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
  private final Set<Player> players = new HashSet<>();
  private int round;
  private final List<Card> cards;
  private int drawIndex;
  private double balance;

  public Game()
  {
    this(new GameConfig());
  }

  public Game(GameConfig config)
  {
    this.config = new GameConfig(config);

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

  public synchronized void join(Player player)
  {
    if (player == null) throw new IllegalArgumentException("player cannot be null");
    if (player.game != this) throw new IllegalArgumentException("player was not configured for this game");
    if (players.size() >= config.getMaxPlayers()) throw new RuntimeException("max Players exceeded");
    players.add(player);
  }

  public synchronized void leave(Player player)
  {
    players.remove(player);
  }

  @Override
  public synchronized void run()
  {
    while (!Thread.interrupted()) {
      playRound();
    }
  }

  public synchronized void run(int rounds)
  {
    int goal = round + rounds;

    while (round < goal) {
      playRound();
    }
  }

  private void playRound()
  {
    final Hand dealersHand;
    final Map<Player, List<Hand>> playerHands = new HashMap<>();
    final Map<Hand, Double> bets = new HashMap<>();
    final List<Hand> showdownHands = new ArrayList<>();

    // init
    {
      players.forEach(Player::handleNewRound);
      dealersHand = new Hand(null);
      for (Player p : players) {
        playerHands.put(p, new ArrayList<>(List.of(new Hand(p))));
      }
    }

    // get bets
    {
      for (List<Hand> hands : playerHands.values()) {
        Hand h = hands.get(0);
        double bet = h.player.placeBet();
        if (bet < config.getBetLimitMin()) throw new IllegalBetException("bet limit undershot");
        if (bet > config.getBetLimitMax()) throw new IllegalBetException("bet limit exceeded");
        bets.put(h, logRevenue(bet));
      }
    }

    // deal cards
    {
      for (List<Hand> hands : playerHands.values()) {
        playerDraws(hands.get(0));
      }
      dealerDraws(dealersHand);
      for (List<Hand> hands : playerHands.values()) {
        playerDraws(hands.get(0));
      }
    }

    // play
    {
      for (Player p : players) {
        showdownHands.addAll(servePlayer(p, playerHands, bets));
      }
      serveDealer(dealersHand);
    }

    // showdown
    {
      List<Card> dealersCards = dealersHand.getCards();
      int dealerCountIdeal = Cards.countIdeal(dealersCards);

      for (Hand hand : showdownHands) {
        List<Card> playersCards = hand.getCards();
        int playerCountIdeal = Cards.countIdeal(playersCards);

        if (Cards.isBlackjack(playersCards) && !Cards.isBlackjack(dealersCards)) {
          hand.player.handleResult(Result.BLACKJACK, logPayout(bets.get(hand) * (1 + config.getBlackjackBonus())));
        } else if (dealerCountIdeal > BLACKJACK) {
          hand.player.handleResult(VICTORY, logPayout(bets.get(hand) * 2));
        } else if (playerCountIdeal == dealerCountIdeal) {
          hand.player.handleResult(TIE, logPayout(bets.get(hand)));
        } else if (playerCountIdeal < dealerCountIdeal) {
          hand.player.handleResult(DEFEAT, logPayout(0));
        } else {
          hand.player.handleResult(VICTORY, logPayout(bets.get(hand) * 2));
        }
      }
    }

    if ((double) drawIndex / cards.size() >= config.getShuffleThreshold()) {
      shuffleCards();
    }

    round++;
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

  private List<Hand> servePlayer(Player player, Map<Player, List<Hand>> playersHand, Map<Hand, Double> bets)
  {
    List<Hand> hands = playersHand.get(player);
    List<Hand> showdownHands = new ArrayList<>();
    boolean isSplit = false;

    for (int i = 0; i < hands.size(); i++) {
      Hand hand = hands.get(i);

      Boolean disqualified = servePlayerHand(hand, hands, bets, isSplit);

      if(disqualified == null) {
        isSplit = true;
        i--;
      } else if(!disqualified) {
        showdownHands.add(hand);
      }
    }

    return showdownHands;
  }

  private Boolean servePlayerHand(Hand hand, List<Hand> hands, Map<Hand, Double> bets, boolean isSplit) {
    Player p = hand.player;
    boolean firstMove = true;
    while (true) {
      switch (p.play()) {
        case STAND:
          return false;

        case SURRENDER:
          if (!firstMove) throw new IllegalMoveException("player can only surrender on first move");
          if(isSplit) throw new IllegalMoveException("player cannot surrender in split");
          p.handleResult(SURRENDER, logPayout(bets.get(hand) * 0.5));
          return true;

        case SPLIT:
          if (!firstMove) throw new IllegalMoveException("player can only split on first move");
          Hand splitHand = hand.split();
          hands.add(splitHand);
          bets.put(splitHand, bets.get(hand));
          return null;

        case DOUBLE:
          if (!firstMove) throw new IllegalMoveException("player can only double on first move");
          bets.put(hand, bets.get(hand) * 2);
          playerDraws(hand);
          if (Cards.isBusted(hand.getCards())) {
            p.handleResult(BUST, logPayout(0));
            return true;
          }
          return false;

        case HIT:
          playerDraws(hand);
          if (Cards.isBusted(hand.getCards())) {
            p.handleResult(BUST, logPayout(0));
            return true;
          }
          break;

        default:
          throw new RuntimeException("wtf");
      }
      firstMove = false;
    }
  }

  private void serveDealer(Hand dealersHand)
  {
    while (true) {
      dealerDraws(dealersHand);
      List<Card> dealersCards = dealersHand.getCards();

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

  private Card playerDraws(Hand hand)
  {
    Card c = draw();
    hand.add(c);
    hand.player.handleDraw(c, hand.player);
    return c;
  }

  private Card dealerDraws(Hand hand)
  {
    Card c = draw();
    hand.add(c);
    for (Player p : players) {
      p.handleDealerDraw(c);
    }
    return c;
  }

  private double logRevenue(double revenue) {
    balance += revenue;
    return revenue;
  }

  private double logPayout(double payout) {
    balance -= payout;
    return payout;
  }

  public Set<Player> getPlayers()
  {
    return Collections.unmodifiableSet(players);
  }

  public int getRound()
  {
    return round;
  }
}
