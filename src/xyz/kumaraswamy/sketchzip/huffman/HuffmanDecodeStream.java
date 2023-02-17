package xyz.kumaraswamy.sketchzip.huffman;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class HuffmanDecodeStream extends InputStream {

  private final InputStream stream;
  private Node binaryTree, root;

  private final byte[] buffer = new byte[5];

  private int readIndex = 0, cursor = 0, streamIndex = 0;

  private int numberOfBytes;

  private boolean nearEnd = false;

  // at the end, we use some special
  // padding thus we would need to modify it
  private int maxReading = 8;

  public HuffmanDecodeStream(InputStream stream) {
    this.stream = stream;
  }


  public void decode() throws IOException {
    root = decodeToBinaryTree();

    numberOfBytes = stream.available();
    this.binaryTree = root;

    streamIndex++;
    firstUnsignedToBits(stream.read());
  }

  // converts the unsigned decimal to
  // bits. Ignores the first bit (padding) of
  // the number

  private void firstUnsignedToBits(int n) {
    int next = n / 2;
    if (next == 0) // avoid the first bit of `n`
      return;
    firstUnsignedToBits(next);
    useBit(n);
  }

  // unsigned decimal to bits, adds extra
  // '0' padding if not of len 8

  private void unsignedToBits(int n, int len) {
    if (n == 0) {
      for (int i = len; i < maxReading; i++)
        // it is like extra '0' padding
        if ((binaryTree = binaryTree.left) instanceof Leaf leaf) {
          buffer[this.cursor++] = leaf.b;
          binaryTree = root;
        }
      return;
    }
    unsignedToBits(n / 2, len + 1);
    useBit(n);
  }

  private void useBit(int n) {
    binaryTree = n % 2 == 0 ? binaryTree.left : binaryTree.right;
    if (binaryTree instanceof Leaf leaf) {
      buffer[cursor++] = leaf.b;
      binaryTree = root;
    }
  }

  @Override
  public int read() throws IOException {
    if (nearEnd && readIndex == cursor)
      return -1;
    if (cursor == 0 || readIndex == cursor) {
      cursor = 0;
      // last second!, indicates the bit length
      // of the last byte
      if (streamIndex == numberOfBytes - 2) {
        maxReading = stream.read(); // unsigned is okay, maximum range 1-8
        streamIndex++;
      }
      if (streamIndex < numberOfBytes - 1)
        while (cursor == 0) {
          streamIndex++;
          unsignedToBits(stream.read(), 0);
        }
      // else almost reached the end
      else {
        // div by 2 removes the last
        // bit
        unsignedToBits(stream.read() / 2, 0);
        nearEnd = true;
      }
      readIndex = 0;
    }
    return buffer[readIndex++] & 0xff;
  }



//  public abstract void write(byte b);

  private Node decodeToBinaryTree() throws IOException {
    if ((byte) stream.read() == 1)
      return new Leaf(0, (byte) stream.read());
    return new Node(decodeToBinaryTree(), decodeToBinaryTree());
  }
}
