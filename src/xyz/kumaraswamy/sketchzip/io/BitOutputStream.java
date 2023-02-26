package xyz.kumaraswamy.sketchzip.io;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream extends OutputStream {

  private final OutputStream output;

  private int currentByte;
  public int bitsWritten = 0;

  public BitOutputStream(OutputStream output) {
    this.output = output;
  }

  /**
   * Appends a bit to the current byte, writes the byte to stream
   * and resets it once reached len 8
   */

  public void writeBit(int b) throws IOException {
    currentByte = (currentByte << 1) | b;
    bitsWritten++;
    if (bitsWritten == 8) {
      output.write(currentByte);

      bitsWritten = 0;
      currentByte = 0;
    }
  }

  /**
   * Converts the @param b to bits and writes to the stream
   *
   * @param n byte to be written
   */

  public void write(int n) throws IOException {
    // un-sign it
    toBits(n & 0xff, 0);
  }


  public void writeInt32(int n) throws IOException {
    write(n >> 24);
    write(n >> 16);
    write(n >> 8);
    write(n);
  }

  /**
   * Writes the decimal bit by bit,
   * pads with extra 0s at the start
   * to complete 8 bits
   */

  private void toBits(int n, int numOfBits) throws IOException {
    if (n == 0) {
      // msb padding
      numOfBits = 8 - numOfBits;
      while (numOfBits-- != 0)
        writeBit(0);
      return;
    }
    toBits(n >> 1, numOfBits + 1);
    writeBit(n % 2);
  }

  public void close() throws IOException {
    if (bitsWritten != 0)
      output.write(currentByte);
  }
}
