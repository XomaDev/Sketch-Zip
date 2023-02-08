package xyz.kumaraswamy.sketchzip.structures;

public final class Block {

  public final SketchList list;

  public final byte dictPoint;
  public final int onset;
  public final int offset;

  public Block(byte[] block, byte dictPoint, int onset, int offset) {
    this.list = new SketchList(block);
    this.dictPoint = dictPoint;
    this.onset = onset;
    this.offset = offset;
  }

  @Override
  public String toString() {
    return "Block[" +
            "block=" + list + ", " +
            "onset=" + onset + ", " +
            "offset=" + offset + ']';
  }
}
