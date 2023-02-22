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

  // TODO:
  //  maybe we should first pass huffman stream - > then to lzw
  //  for faster compression
  private int currentByte;

  private int bitsWritten = 0;

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

    writeBit(1);
    bitsWritten++;

    for (byte b : bytes)
      dumpBits(paths[b & 0xff]);
    stream.write(bitsWritten);
    writeBit(1);

    push();
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
    currentByte = (currentByte << 1) | bit;
    bitsWritten++;

    if (bitsWritten == 8)
      push();
  }

  private void push() throws IOException {
    stream.write(currentByte);
    bitsWritten = 0;
    currentByte = 0;
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