package xyz.kumaraswamy.sketchzip.decoder;

import xyz.kumaraswamy.sketchzip.Pencil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Decoder {

  private final List<byte[]> dictionary;

  private byte dictByteRep;

  static class SketchException extends IOException {
    public SketchException(String message) {
      super(message);
    }
  }

  private final InputStream stream;
  private final OutputStream outputStream;

  public Decoder(InputStream stream, OutputStream outputStream) throws IOException {
    this.stream = stream;
    this.outputStream = outputStream;
    matchSignature();

    // sz[range headers length][dictionary length]{range headers}{dictionary}{content}

    int rangeHeadersSize = readNextInt();
    int dictionarySize = readNextInt();

    System.out.println("range headers size = " + rangeHeadersSize);
    System.out.println("dictionary size = " + dictionarySize);

    int numberOfRanges = rangeHeadersSize / 5;
    byte[] ranges = new byte[numberOfRanges];
    int[] rangeOffsets = new int[numberOfRanges];

    for (int i = 0, r = 0; i < rangeHeadersSize; i++, r++) {
      ranges[r] = (byte) stream.read();
      rangeOffsets[r] = readNextInt();
      i += 4; // we read offsets
    }
//    System.out.println("ranges = " + Arrays.toString(ranges));
//    System.out.println("offsets = " + Arrays.toString(rangeOffsets));

    dictionary = new ArrayList<>();

    for (int i = 0; i < dictionarySize; i++) {
      int wordLen = stream.read() & 0xff;

      byte[] word = new byte[wordLen];
      for (int j = 0; j < wordLen; j++)
        word[j] = (byte) stream.read();
      dictionary.add(word);
      i += wordLen;
    }
//    dictionary.forEach(bytes -> System.out.println("a word = " + new String(bytes)));
    // the rest is mystery xD

    for (int i = 0; i < numberOfRanges; i++) {
      dictByteRep = ranges[i];
      System.out.println("dict rep = " + dictByteRep);
      int offset = rangeOffsets[i];
      int len = 0;

      int read;
      while ((read = stream.read()) != -1) {
        byte b = (byte) read;
        if (b == dictByteRep)
          readWord(readNextIndex());
        else outputStream.write(read);
        if (++len == offset)
          break;
      }
//      System.out.println("len = " + len + "---------------");
    }
//    System.out.println("is still available = " + stream.available());
  }

  public void readWord(int index) throws IOException {
    byte[] bytes = dictionary.get(index - 32768);
    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      //        System.out.println("b = " + b);
      //        System.out.println("found at = " + Arrays.toString(bytes));
      if (b == dictByteRep) readWord(toShort(bytes[++i], bytes[++i]));
      else outputStream.write(b);
    }
  }

  /**
   * reads a 16 bit short from the stream
   */
  private int readNextIndex() throws IOException {
    return toShort((byte) stream.read(), (byte) stream.read());
  }

  private int toShort(byte first, byte second) {
    return (first & 255) << 8 |
            second & 255;
  }


  /**
   * reads 32 bit int from the stream
   *
   * @return int
   */

  private int readNextInt() throws IOException {
    return (((byte) stream.read() & 255) << 24) |
            (((byte) stream.read() & 255) << 16) |
            (((byte) stream.read() & 255) << 8) |
            (((byte) stream.read() & 255));
  }

  private void matchSignature() throws IOException {
    for (byte b : Pencil.NAME)
      if (b != stream.read())
        throw new SketchException("Not a valid Sketch-Zip file!");
  }
}
