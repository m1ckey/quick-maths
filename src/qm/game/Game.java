package qm.game;

import qm.game.card.Card;
import qm.game.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game
    implements Runnable
{
  private final GameConfig config;
  private final Player[] players;
  private long round;
  private List<Card> undrawnCards;

  public Game()
  {
    this(new GameConfig());
  }

  public Game(GameConfig config)
  {
    this.config = new GameConfig(config);
    players = new Player[config.getMaxPlayers()];
    undrawnCards = new ArrayList<>(config.getDecks() * config.getDeck().size());
    shuffleCards();
  }

  private void shuffleCards()
  {
    undrawnCards.clear();

    for (int i = 0; i < config.getDecks(); i++) {
      undrawnCards.addAll(config.getDeck());
    }

    Collections.shuffle(undrawnCards);
  }

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

  @Override
  public synchronized void run()
  {
    // todo: play round
  }
}
