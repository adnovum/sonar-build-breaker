package com.acme.basic;

public class HelloWorld {

  // Cause bad issues on purpose so project fails Quality Gate and triggers the build breaker.
  public static final String Deliberate__BadlyFormattedPasswordVar = "dummypassword";

  void sayHello() {
    System.out.println("Hello World!");
  }

  void notCovered() {
    System.out.println("This method is not covered by unit tests");
  }

}
