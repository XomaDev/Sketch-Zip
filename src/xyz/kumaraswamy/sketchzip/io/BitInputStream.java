package xyz.kumaraswamy.sketchzip.io;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream extends InputStream {

  private final InputStream stream;

  private int currentInt = 0;
  private int bitCursor = -1;

  public BitInputStream(InputStream stream) {
    this.stream = stream;
  }

  public int available() throws IOException {
    int available = stream.available();
    if (bitCursor != -1)
      available++;
    return available;
  }

  public int readShort16() throws IOException {
    return ((byte) read() & 255) << 8 |
            (byte) read() & 255;
  }

  /**
   * Reads a 32-bit integer from the stream
   */

  public int readInt32() throws IOException {
    return (((byte) read() & 255) << 24) |
            (((byte) read() & 255) << 16) |
            (((byte) read() & 255) << 8) |
            (((byte) read() & 255));
  }

  /**
   * Reads the next bit from the stream
   */
  public int readBit() throws IOException {
    if (bitCursor == -1) { // no buffer
      currentInt = stream.read();
      if (currentInt == -1)
        return -1;
      bitCursor = 7;
    }
    // shift the number right n times
    // then find the LSB of it
    return (currentInt >> bitCursor--) & 1;
  }

  /**
   * Reads the next 8 bits to form a byte
   * @return a byte
   */

  public int read() throws IOException {
    int n = 0;
    for (int i = 0; i < 8; i++) {
      int bit = readBit();
      if (bit == -1)
        break;
      n = (n << 1) | bit;
    }
    return n;
  }

}
