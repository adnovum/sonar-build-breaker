package com.acme.basic;

public class HelloWorld {

  void sayHello() {
    System.out.println("Hello World!");
    // Deliberate bug (java:S1217) so project fails Quality Gate and triggers the build breaker.
    new Thread().run();
  }

  void notCovered() {
    System.out.println("This method is not covered by unit tests");
  }

}
