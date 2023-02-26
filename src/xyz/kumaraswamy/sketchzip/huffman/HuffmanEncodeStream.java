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

  public HuffmanEncodeStream(OutputStream stream) throws IOException {
    this.stream = new BitOutputStream(stream);
    stream.write(new byte[] { 104, 122 }); // signature
  }

  public void allocate(int cap) throws IOException {
    bytes = new byte[cap];
    stream.writeInt32(cap);
  }

  @Override
  public void write(int i) {
    byte b = (byte) i;
    bytes[index++] = b;
    frequencies[b & 0xff]++;
  }

  public void writeTree(Node node) throws IOException {
    if (node.left == null) { // leaf node
      stream.writeBit(1);
      stream.write(node.b);
    } else {
      stream.writeBit(0);
      writeTree(node.left);
      writeTree(node.right);
    }
  }

  public int pathOf(byte b, Node node, int path) {
    if (node.left == null) // leaf node
      return node.b == b ? path : -1;
    path <<= 1;

    int p = pathOf(b, node.left, path | 1);
    return p == -1 ? pathOf(b, node.right, path) : p;
  }

  public void encode() throws IOException {
    FrequencyQueue queue = new FrequencyQueue(256);

    for (int i = 0; i < Pencil.BYTES_LIMIT; i++) {
      int freq = frequencies[i];
      if (freq != 0)
        queue.add(new Node((byte) i, freq));
    }

    Node root = queue.poll();

    stream.writeBit(1); // preservation
    writeTree(root);

    int[] paths = new int[Pencil.BYTES_LIMIT];

    for (int i = 0; i < Pencil.BYTES_LIMIT; i++)
      if (frequencies[i] != 0)
        // has extra 1 bit MSB
        paths[i] = pathOf((byte) i, root, 1);

    for (byte b : bytes)
      writeBits(paths[b & 0xff]);

    // allow decoder to terminate without
    // problems
    for (int i = 0; i < 2; i++)
      stream.write(0);
    stream.close();
  }

  // writes the path to the stream,
  // ignores msb

  private void writeBits(int path) throws IOException {
    int next = path >> 1;
    if (next == 0)
      return;
    writeBits(next);
    stream.writeBit(path % 2);
  }
}
