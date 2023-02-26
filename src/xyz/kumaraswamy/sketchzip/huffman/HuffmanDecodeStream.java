package xyz.kumaraswamy.sketchzip.huffman;

import xyz.kumaraswamy.sketchzip.Pencil;
import xyz.kumaraswamy.sketchzip.io.BitInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HuffmanDecodeStream extends InputStream {

  private static int BUFFER_SIZE = 5;
  private final byte[] buffer = new byte[BUFFER_SIZE];

  private int readCursor, writeCursor;

  private final BitInputStream bitStream;

  private final int originalSize;
  private int bytesDecoded = 0;

  private boolean reachedEnd = false;

  private final Node root;
  private Node traveller;

  public HuffmanDecodeStream(InputStream stream) throws IOException {
    bitStream = new BitInputStream(stream);

    for (byte b : Pencil.HUFFMAN)
      if (b != bitStream.read())
        throw new IOException("Not a huffman file!");

    originalSize = bitStream.readInt32();
    bitStream.readBit(); // ignore reading

    root = decodeHuffmanTree();
    traveller = root;
  }

  @Override
  public byte[] readAllBytes() throws IOException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    int read;
    while ((read = read()) != -1)
      stream.write(read);
    return stream.toByteArray();
  }

  @Override
  public int read() throws IOException {
    if (writeCursor == 0 || readCursor == BUFFER_SIZE) {
      if (reachedEnd)
        return -1;
      writeCursor = 0;
      while (writeCursor != BUFFER_SIZE) {
        traveller = bitStream.readBit() == 1
                ? traveller.left
                : traveller.right;
        if (traveller.left == null) {
          buffer[writeCursor++] = traveller.b;
          traveller = root;

          bytesDecoded++;
          if (bytesDecoded == originalSize) {
            // reached the end, no more bytes to decode
            // set the buffer size to match the last bytes
            BUFFER_SIZE = writeCursor;
            reachedEnd = true;
            break;
          }
        }
      }
      readCursor = 0;
    }
    return buffer[readCursor++];
  }


  private Node decodeHuffmanTree() throws IOException {
    if (bitStream.readBit() == 1)
      return new Node((byte) bitStream.read(), 0);
    return new Node(decodeHuffmanTree(), decodeHuffmanTree());
  }
}
