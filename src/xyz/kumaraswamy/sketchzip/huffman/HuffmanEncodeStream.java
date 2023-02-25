package xyz.kumaraswamy.sketchzip.huffman;

import xyz.kumaraswamy.sketchzip.Pencil;
import xyz.kumaraswamy.sketchzip.io.BitOutputStream;
import xyz.kumaraswamy.sketchzip.structures.FrequencyQueue;

import java.io.IOException;
import java.io.OutputStream;

public class HuffmanEncodeStream extends OutputStream {

  private final BitOutputStream stream;

  private byte[] bytes;
  private int index = 0;

  private final int[] frequencies = new int[Pencil.BYTES_LIMIT];

  public HuffmanEncodeStream(OutputStream stream) {
    this.stream = new BitOutputStream(stream);
  }

  public void allocate(int cap) {
    bytes = new byte[cap];
  }

  @Override
  public void write(int i) {
    byte b = (byte) i;
    bytes[index++] = b;
    frequencies[b & 0xff]++;
  }

  /**
   * Writes the tree to the stream.
   * Each leaf is represent by *1* bit followed
   * by next character.
   * <p>
   * A tree: which has left and right element is
   * represented by *0* bit, and it goes on
   */
  private void dumpTree(Node node) throws IOException {
    // [0, 1, 2, 1, 3]
    if (node instanceof Leaf) {
      stream.writeBit(1);
      stream.write(((Leaf) node).b);
      return;
    }
    stream.writeBit(0);
    dumpTree(node.left);
    dumpTree(node.right);
  }

  public void encode() throws IOException {
    FrequencyQueue queue = new FrequencyQueue(256);

    for (int i = 0; i < Pencil.BYTES_LIMIT; i++) {
      int freq = frequencies[i];
      if (freq == 0)
        continue;
      queue.add(new Leaf(freq, (byte) i));
    }

    Node root = queue.poll();
    dumpTree(root);

    int[] paths = new int[Pencil.BYTES_LIMIT];

    for (int i = 0; i < Pencil.BYTES_LIMIT; i++) {
      int freq = frequencies[i];
      if (freq == 0)
        continue;
      // the binary value assigned
      // to the byte
      int bPath = findPath((byte) i, root, 1);

      int pathDiv = bPath;
      while (pathDiv != 0)
        pathDiv >>= 1;
      paths[i] = bPath;
    }

    stream.writeBit(1); // initial padding
    for (byte b : bytes)
      dumpBits(paths[b & 0xff]);

    // indicates how many buffer remaining
    // or the total len of last byte.
    // can be 0 too. Writes the value directly to
    // the stream, irrespective of whether there
    // is a buffer already.
    stream.writeStream(stream.bitsWritten + 1);
    stream.writeBit(1); // end padding

    stream.close();
  }

  /**
   * Writes the decimal n as bits to the stream,
   * ignores the MSB
   */
  private void dumpBits(int n) throws IOException {
    int next = n >> 1;
    if (next == 0)
      return;
    dumpBits(next);
    stream.writeBit(n % 2);
  }

  private int findPath(byte b, Node node, int path) {
    if (node instanceof Leaf) {
      if (((Leaf) node).b == b)
        return path;
      return -1;
    }
    path <<= 1;
    int p = findPath(b, node.right, path | 1);
    if (p == -1)
      p = findPath(b, node.left, path);
    return p;
  }
}
