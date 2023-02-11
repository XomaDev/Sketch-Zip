package xyz.kumaraswamy.sketchzip.huffman;

public class Leaf extends Node {

    public final byte b;

    public Leaf(int freq, byte b) {
      super(freq);
      this.b = b;
    }

    @Override
    public String toString() {
      return "Leaf{" +
              "byt=" + (char) b +
              '}';
    }
  }