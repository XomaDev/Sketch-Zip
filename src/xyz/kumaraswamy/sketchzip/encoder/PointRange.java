package xyz.kumaraswamy.sketchzip.encoder;

import xyz.kumaraswamy.sketchzip.Pencil;
import xyz.kumaraswamy.sketchzip.structures.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PointRange {
  private final byte[] points = new byte[Pencil.BYTES_LIMIT];
  private byte[] bytes;

  private int len = 0;

  public PointRange() {

  }

  public PointRange(byte[] bytes) {
    this.bytes = bytes;
    for (byte b : bytes)
      add(b);
  }

  public void add(byte b) {
    if (len == Pencil.BYTES_LIMIT || has(b))
      return;
    points[len++] = b;
  }

  public boolean hasRoom() {
    return len + 1 < Pencil.BYTES_LIMIT;
  }

  private byte getPoint() {
    for (int i = Byte.MIN_VALUE;
         i <= Byte.MAX_VALUE;
         i++)
      if (!has((byte) i))
        return (byte) i;
    throw new RuntimeException("Ah");
  }

  private boolean has(byte b) {
    for (int i = 0; i < len; i++)
      if (points[i] == b)
        return true;
    return false;
  }

  public List<Block> generateRanges() {
    List<Block> blocks = new ArrayList<>();

    int onset = 0, offset = 0;
    int len = bytes.length;

    PointRange range = new PointRange();

    while (offset < len) {
      for (offset = onset;
           offset < len && range.hasRoom();
           offset++)
        range.add(bytes[offset]);

      byte dictPoint = range.getPoint();
      byte[] block = offset == len
              ? bytes
              : Arrays.copyOfRange(bytes, onset, offset);

      blocks.add(new Block(block, dictPoint));
      System.out.printf("Range %d:%d, dict_point: %d,\n", onset,
              offset, dictPoint);
      range = new PointRange();
      onset = offset;
    }
    return blocks;
  }
}
