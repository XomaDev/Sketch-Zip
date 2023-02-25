package xyz.kumaraswamy.sketchzip.huffman;

import xyz.kumaraswamy.sketchzip.io.BitInputStream;

import java.io.IOException;
import java.io.InputStream;

public class HuffmanDecodeStream extends InputStream {

  private final BitInputStream stream;
  private Node binaryTree, root;

  private final byte[] buffer = new byte[5];

  private int readIndex = 0, cursor = 0, streamIndex = 0;

  private int numberOfBytes;

  private boolean nearEnd = false;

  // at the end, we use some special
  // padding thus we would need to modify it
  private int maxReading = 8;

  public HuffmanDecodeStream(InputStream stream) {
    this.stream = new BitInputStream(stream);
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
    int next = n >> 1;
    if (next == 0) // avoid the first bit of `n`
      return;
    firstUnsignedToBits(next);
    useBit(n);
  }

  // unsigned decimal to bits, adds extra
  // '0' padding if not of len 8

  private void unsignedToBits(int n, int len) {
    if (n == 0) {
      len = maxReading - len;
      while (len-- != 0) {
        if ((binaryTree = binaryTree.left) instanceof Leaf leaf) {
          buffer[this.cursor++] = leaf.b;
          binaryTree = root;
        }
      }
      return;
    }
    unsignedToBits(n >> 1, len + 1);
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
        unsignedToBits(stream.read(), 0);
        nearEnd = true;
      }
      readIndex = 0;
    }
    return buffer[readIndex++] & 0xff;
  }

  private Node decodeToBinaryTree() throws IOException {
    if (stream.readBit() == 1)
      return new Leaf(0, (byte) stream.read());
    return new Node(decodeToBinaryTree(), decodeToBinaryTree());
  }
}
