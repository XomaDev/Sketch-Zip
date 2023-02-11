package xyz.kumaraswamy.sketchzip.decoder;

import xyz.kumaraswamy.sketchzip.Pencil;
import xyz.kumaraswamy.sketchzip.huffman.HuffmanDecodeStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Decoder {

  private final List<byte[]> dictionary;

  private byte dictByteRep;

  static class SketchException extends IOException {
    public SketchException(String message) {
      super(message);
    }
  }

  private InputStream stream;
  private final OutputStream outputStream;

  public Decoder(InputStream stream, OutputStream outputStream) throws IOException {
    this.stream = stream;
    this.outputStream = outputStream;

    matchSignature();

    if (true) {
      ByteArrayOutputStream decoded = new ByteArrayOutputStream();
      HuffmanDecodeStream decodeStream = new HuffmanDecodeStream(stream) {
        @Override
        public void write(byte b) {
          decoded.write(b);
        }
      };
      System.out.println("decoded = " + decoded);
      decodeStream.decode();
      stream = new ByteArrayInputStream(decoded.toByteArray());
      this.stream = stream;
    }

    // sz[range headers length][dictionary length]{range headers}{dictionary}{content}

    int rangeHeadersSize = readNextInt();
    int dictionarySize = readNextInt();

    System.out.println("range headers size = " + rangeHeadersSize);
    System.out.println("dictionary size = " + dictionarySize);

    int numberOfRanges = rangeHeadersSize / 5;
    byte[] ranges = new byte[numberOfRanges];
    int[] rangeOffsets = new int[numberOfRanges];

    // TODO:
    //  improvements
    for (int i = 0, r = 0; i < rangeHeadersSize; i++, r++) {
      ranges[r] = (byte) stream.read();
      rangeOffsets[r] = readNextInt();
      i += 4; // we read offsets
    }

    dictionary = new ArrayList<>();

    for (int i = 0; i < dictionarySize; i++) {
      int wordLen = stream.read() & 0xff;

      byte[] word = new byte[wordLen];
      for (int j = 0; j < wordLen; j++)
        word[j] = (byte) stream.read();
      dictionary.add(word);
      i += wordLen;
    }

    for (int i = 0; i < numberOfRanges; i++) {
      dictByteRep = ranges[i];
      int offset = rangeOffsets[i];
      int len = 0;

      int read;
      while ((read = stream.read()) != -1) {
        if ((byte) read == dictByteRep)
          readWord(readNextIndex());
        else outputStream.write(read);
        if (++len == offset)
          break;
      }
    }
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
