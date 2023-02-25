package xyz.kumaraswamy.sketchzip.io;

import org.junit.jupiter.api.Test;


import java.io.ByteArrayInputStream;
import java.io.IOException;

class BitInputStreamTest {
  @Test
  public void testBitReading() throws IOException {
    byte[] bytes = {27};

    // 11011
    ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
    BitInputStream bis = new BitInputStream(stream);

    int b = 0;

    int bit;
    while ((bit = bis.readBit()) != -1) {
      System.out.println("bit = " + bit);
      b = (b << 1) | bit;
    }
    System.out.println(b);
  }

  @Test
  public void testByteReading() throws IOException {
    byte[] bytes = {27, 28};

    // 11011
    ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
    BitInputStream bis = new BitInputStream(stream);

    System.out.println(bis.read());
    System.out.println(bis.read());
  }
}