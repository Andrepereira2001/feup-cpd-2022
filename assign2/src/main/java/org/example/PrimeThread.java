package org.example;

public class PrimeThread extends Thread /*(or we can) implements Runnable */{
    long minPrime;
    PrimeThread(long minPrime){
        this.minPrime = minPrime;
    }

    // necessary to overwrite
    public void run() {

    }
}

/* To execute the thread

//extends Thread

PrimeThread p = new PrimeThread(11);
p.start()

//implements Runnable

PrimeThread p = new PrimeThread(11);
new Thread(p).start();

 */