package xyz.kumaraswamy.sketchzip.decoder;

import xyz.kumaraswamy.sketchzip.Pencil;
import xyz.kumaraswamy.sketchzip.huffman.HuffmanDecodeStream;
import xyz.kumaraswamy.sketchzip.huffman.HuffmanEncodeStream;

import java.io.ByteArrayOutputStream;
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

  private final HuffmanDecodeStream huffmanStream;
  private final OutputStream outputStream;

  public Decoder(InputStream inputStream, OutputStream outputStream) throws IOException {
    matchSignature(inputStream); // match the signature first!

    huffmanStream = new HuffmanDecodeStream(inputStream);
    huffmanStream.decode();

    inputStream = this.huffmanStream;

    this.outputStream = outputStream;


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
      ranges[r] = (byte) inputStream.read();
      rangeOffsets[r] = readNextInt();
      i += 4; // we read offsets
    }

    dictionary = new ArrayList<>();
    for (int i = 0; i < dictionarySize; i++) {
      int wordLen = inputStream.read() & 0xff;

      byte[] word = new byte[wordLen];
      for (int j = 0; j < wordLen; j++)
        word[j] = (byte) inputStream.read();
      dictionary.add(word);
      i += wordLen;
    }

    int len = 0;
    for (int i = 0; i < numberOfRanges; i++) {
      dictByteRep = ranges[i];
      int offset = rangeOffsets[i];

      int read;
      while ((read = inputStream.read()) != -1) {
        if (((byte) read) == dictByteRep)
          readWord(readNextIndex());
        else outputStream.write(read);
        if (++len == offset) // read every 255 chunks of byte
          break;
      }
    }
  }

  public void readWord(int index) throws IOException {
    byte[] bytes = dictionary.get(index - 32768);
    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      if (b == dictByteRep)
        readWord(toShort(bytes[++i], bytes[++i]));
      else outputStream.write(b);
    }
  }

  /**
   * reads a 16 bit short from the stream
   */
  private int readNextIndex() throws IOException {
    return toShort((byte) huffmanStream.read(), (byte) huffmanStream.read());
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
    return (((byte) huffmanStream.read() & 255) << 24) |
            (((byte) huffmanStream.read() & 255) << 16) |
            (((byte) huffmanStream.read() & 255) << 8) |
            (((byte) huffmanStream.read() & 255));
  }

  private void matchSignature(InputStream stream) throws IOException {
    for (byte b : Pencil.NAME)
      if (b != stream.read())
        throw new SketchException("Not a valid Sketch-Zip file!");
  }
}
