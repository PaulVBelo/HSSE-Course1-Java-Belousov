package org.example;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class Player implements Runnable {
  private CountDownLatch countDownLatch;
  private int id;
  private int score = 0;
  private Set<Integer> generatedNumbers = new HashSet<>();
  public static volatile Set<Integer> allGeneratedNumbers = new HashSet<>();
  private Logger logger = Logger.getLogger(Player.class.getName());
  private static volatile boolean gameEnded = false;

  public int getScore() {
    return this.score;
  }

  public Player(int id, CountDownLatch countDownLatch) {
    this.id = id;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    Random random = new Random();
    while (generatedNumbers.size() < 100 && !gameEnded) {
      int number = random.nextInt(0, 100) + 1;
      if (generatedNumbers.add(number)) {
        logger.info(Thread.currentThread().getName() + " сгенерировал впервые число " + number);
        if (allGeneratedNumbers.add(number)) {
          this.score++;
          logger.info(Thread.currentThread().getName() + " опередил остальных, сгенерировав число " + number);
        }
      }


    }
    if (gameEnded) {
      logger.info(Thread.currentThread().getName() + " не успел сгенерировать все числа. Какая досада!");
    } else {
      gameEnded = true;
      logger.info(Thread.currentThread().getName() + " сгенерировал все числа. Завершаем игру!");
    }
    countDownLatch.countDown();
  }
}
