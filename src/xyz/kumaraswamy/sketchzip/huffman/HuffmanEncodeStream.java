package xyz.kumaraswamy.sketchzip.huffman;

import xyz.kumaraswamy.sketchzip.Pencil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.PriorityQueue;

public class HuffmanEncodeStream extends OutputStream {

  private final OutputStream stream;

  private byte[] bytes;
  private int index = 0;

  private final int[] frequencies = new int[Pencil.BYTES_LIMIT];

  // true indicates 1,
  // and false indicates 0, saves memory, int - > 32 bits
  // boolean - > 1 bit representation
  private final boolean[] bits = new boolean[8];
  private int bitIndex = 0;

  public HuffmanEncodeStream(OutputStream stream) {
    this.stream = stream;
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

  public void encode() throws IOException {
    PriorityQueue<Node> queue = new PriorityQueue<>();

    // len required to store tree in bytes

    for (int i = 0; i < Pencil.BYTES_LIMIT; i++) {
      int freq = frequencies[i];
      if (freq == 0)
        continue;
      queue.add(new Leaf(freq, (byte) i));
    }
    while (queue.size() > 1)
      queue.add(new Node(queue.poll(), queue.poll()));

    Node root = queue.poll();
    dumpTree(root);

    // TODO:
    //  we can use short data type
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
        pathDiv /= 2;
      paths[i] = bPath;
    }

    bits[0] = true; // padding, true - > binary 1
    bitIndex++;

    for (byte b : bytes)
      dumpBits(paths[b & 0xff]);
    stream.write(bitIndex);
    bits[bitIndex++] = true;  // padding

    stream.write((byte) toByte(bitIndex, bits));

    // [content size] [content] [tree]
  }

  private void dumpBits(int n) throws IOException {
    int next = n / 2;
    if (next == 0)
      return;
    dumpBits(next);
    writeBit(n % 2 == 1);
  }

  private void dumpTree(Node node) throws IOException {
    // [0, 1, 2, 1, 3]
    if (node instanceof Leaf) {
      stream.write(1);
      stream.write(((Leaf) node).b);
      return;
    }
    // TODO:
    //  we can surely improve this
    //  write bits instead of single bytes
    stream.write(0);
    dumpTree(node.left);
    dumpTree(node.right);
  }

  private void writeBit(boolean bit) throws IOException {
    bits[bitIndex++] = bit;
    if (bitIndex == 8) {
      stream.write((byte) toByte(bitIndex, bits));
      bitIndex = 0;
    }
  }

  public static int toByte(int len, boolean... bits) {
    int total = 0;
    for (int i = len - 1, pow = 0; i >= 0; i--, pow++)
      total += (1 << pow) * (bits[i] ? 1 : 0);
    return total;
  }

  private int findPath(byte b, Node node, int path) {
    if (node instanceof Leaf) {
      if (((Leaf) node).b == b)
        return path;
      return -1;
    }
    int p = findPath(b, node.right, (path << 1) + 1);
    if (p == -1)
      p = findPath(b, node.left, path << 1);
    return p;
  }
}
