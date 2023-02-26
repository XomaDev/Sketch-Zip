package xyz.kumaraswamy.sketchzip;

public class Pencil {
  public static final int MINIMUM_WORD_SIZE = 5;
  public static final int MAXIMUM_WORD_SIZE = 10;

  public static final int BYTES_LIMIT = Math.negateExact(
          Byte.MIN_VALUE) + Byte.MAX_VALUE + 1; // +1 for 0

  public static final byte[] NAME = {  115, 122  };
  public static final byte[] HUFFMAN = {  104, 122  };
}
