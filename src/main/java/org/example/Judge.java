package org.example;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

public class Judge implements Runnable {
  CountDownLatch latch = new CountDownLatch(2);

  Map<String, Integer> results = new HashMap<>();

  @Override
  public void run() {
    Player player1 = new Player(1, latch);
    Thread player1Thread = new Thread(player1);
    player1Thread.setName("Player 1");
    Player player2 = new Player(2, latch);
    Thread player2Thread = new Thread(player2);
    player2Thread.setName("Player 2");
    player1Thread.start();
    player2Thread.start();

    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      results.put("Player 1", player1.getScore());
      results.put("Player 2", player2.getScore());
      int maxScore = Integer.MIN_VALUE;
      List<String> betterPlayers = new ArrayList<>();

      for (Entry<String, Integer> entry : results.entrySet()) {
        if (entry.getValue() >= maxScore) {
          if (entry.getValue() > maxScore) {
            maxScore = entry.getValue();
            betterPlayers.clear();
            betterPlayers.add(entry.getKey());
          } else {
            betterPlayers.add(entry.getKey());
          }
        }
      }
      Collections.sort(betterPlayers);
      String hallOfFame = String.join(", ", betterPlayers);

      if (betterPlayers.size()==1) {
        System.out.println(hallOfFame + " одержал безоговорочную победу со счётом " + maxScore + ".");
      } else {
        System.out.println("В равной борьбе особенно хорошо себя показали " + hallOfFame
            + ". Они достигли счёта " + maxScore + ".");
      }
    }
  }
}
