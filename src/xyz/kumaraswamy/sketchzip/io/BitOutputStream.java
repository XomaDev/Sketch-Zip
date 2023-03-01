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
    n &= 0xff; // un-sign it

    // range: 0 ~ 256 only
    // convert the ints to bits
    for (int i = 7; i >= 0; i--)
      writeBit((n >> i) & 1);
  }

  /**
   * Writes 32-bit integer to output
   */
  public void writeInt32(int n) throws IOException {
    write(n >> 24);
    write(n >> 16);
    write(n >> 8);
    write(n);
  }


  /**
   * Writes a 16-bit short int to stream
   */
  public void writeShort16(short n) throws IOException {
    write(n >> 8);
    write((byte) n);
  }

  public void close() throws IOException {
    if (bitsWritten != 0)
      output.write(currentByte);
  }
}
