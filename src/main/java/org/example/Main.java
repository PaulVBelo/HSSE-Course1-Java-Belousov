package org.example;

public class Main {
  public static void main(String[] args) {
    Judge judge = new Judge();
    Thread judgeThread = new Thread(judge);
    judgeThread.start();
  }
}