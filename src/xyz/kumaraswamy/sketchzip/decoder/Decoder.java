package xyz.kumaraswamy.sketchzip.decoder;

import xyz.kumaraswamy.sketchzip.Pencil;
import xyz.kumaraswamy.sketchzip.huffman.HuffmanDecodeStream;
import xyz.kumaraswamy.sketchzip.io.BitInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

  private final OutputStream outputStream;

  public Decoder(InputStream inputStream, OutputStream outputStream) throws IOException {
    for (byte b : Pencil.HUFFMAN)
      if (b != inputStream.read())
        throw new SketchException("Not a valid Sketch-Zip file!");

    // Huffman Decode Stream => Bit Stream Wrapper => Decoder
    BitInputStream bitsStream = new BitInputStream(new HuffmanDecodeStream(inputStream));
    this.outputStream = outputStream;


    // sz[range headers length][dictionary length]{range headers}{dictionary}{content}

    int rangeHeadersSize = bitsStream.readInt32();
    int dictionarySize = bitsStream.readInt32();

    System.out.println("range headers size = " + rangeHeadersSize);
    System.out.println("dictionary size = " + dictionarySize);

    int numberOfRanges = rangeHeadersSize / 5;
    byte[] ranges = new byte[numberOfRanges];
    int[] rangeOffsets = new int[numberOfRanges];

    // TODO:
    //  improvements
    for (int i = 0, r = 0; i < rangeHeadersSize; i++, r++) {
      ranges[r] = (byte) bitsStream.read();
      rangeOffsets[r] = bitsStream.readInt32();
      System.out.println("range offset = " + rangeOffsets[r]);
      i += 4; // we read offsets
    }

    dictionary = new ArrayList<>();
    for (int i = 0; i < dictionarySize; i++) {
      int wordLen = bitsStream.read() & 0xff;

      byte[] word = new byte[wordLen];
      for (int j = 0; j < wordLen; j++)
        word[j] = (byte) bitsStream.read();
      dictionary.add(word);
      i += wordLen;
    }

    int len = 0;
    for (int i = 0; i < numberOfRanges; i++) {
      dictByteRep = ranges[i];
      int offset = rangeOffsets[i];
      int read;
      while ((read = bitsStream.read()) != -1) {
        if (((byte) read) == dictByteRep) {
          len += 2; // for the byte indicators
          readWord(bitsStream.readShort16());
        } else outputStream.write(read);
        if (++len == offset)
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

  private int toShort(int first, int second) {
    return (first & 255) << 8 |
            second & 255;
  }
}
