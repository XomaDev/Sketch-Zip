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

  private final int[] bits = new int[8];
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
    dumpTree(root);

    bits[0] = 1; // padding
    bitIndex++;

    for (byte b : bytes)
      dumpBits(paths[b & 0xff]);
    writeBit(1); // padding
    if (bitIndex > 0)
      stream.write((byte) toByte(bitIndex, bits));

    // [content size] [content] [tree]
  }

  private void dumpBits(int n) throws IOException {
    int next = n / 2;
    if (next == 0)
      return;
    dumpBits(next);
    writeBit(n % 2);
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

  private void writeBit(int bit) throws IOException {
    bits[bitIndex++] = bit;
    if (bitIndex == 8) {
//      System.out.println("threshold is full! ");
      stream.write((byte) toByte(bitIndex, bits));
      bitIndex = 0;
    }
  }

  public static int toByte(int len, int... bits) {
    int total = 0;
    for (int i = len - 1, pow = 0; i >= 0; i--, pow++)
      total += (1 << pow) * bits[i];
//    System.out.println("converted = " + (byte) total + " | " + Arrays.toString(bits) + " at index " + len);
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
