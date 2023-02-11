package xyz.kumaraswamy.sketchzip.huffman;

import java.io.IOException;
import java.io.InputStream;

public abstract class HuffmanDecodeStream {

  private final InputStream stream;
  private Node binaryTree, root;

  public HuffmanDecodeStream(InputStream stream) {
    this.stream = stream;
  }

  // TODO:
  //   figure out a way to implement
  //   this class in an actual Stream type
  //   class thus making it more good

  public void decode() throws IOException {
    root = decodeToBinaryTree();
    System.out.println("miw");

    int contentBytes = stream.available();
    this.binaryTree = root;


    firstUnsignedToBits(stream.read());
    //                             -2 because first byte is already red
    //                             and last byte handled specially
    for (int i = 0; i < contentBytes - 2; i++)
      unsignedToBits(stream.read(), 0);

    unsignedToBits(stream.read() / 2);
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
      for (int i = len; i < 8; i++)
        // it is like extra '0' padding
        if ((binaryTree = binaryTree.left) instanceof Leaf leaf) {
          write(leaf.b);
          binaryTree = root;
        }
      return;
    }
    unsignedToBits(n / 2, len + 1);
    useBit(n);
  }

  private void unsignedToBits(int n) {
    if (n == 0) return;
    unsignedToBits(n / 2);
    useBit(n);
  }

  private void useBit(int n) {
    binaryTree = n % 2 == 0 ? binaryTree.left : binaryTree.right;
    if (binaryTree instanceof Leaf leaf) {
      write(leaf.b);
      binaryTree = root;
    }
  }

  public abstract void write(byte b);

  private Node decodeToBinaryTree() throws IOException {
    if ((byte) stream.read() == 1)
      return new Leaf(0, (byte) stream.read());
    return new Node(decodeToBinaryTree(), decodeToBinaryTree());
  }
}
