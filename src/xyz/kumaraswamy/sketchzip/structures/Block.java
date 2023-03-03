package xyz.kumaraswamy.sketchzip.structures;

public final class Block {

  public final SketchList list;

  public final byte dictPoint;

  public Block(byte[] block, byte dictPoint) {
    this.list = new SketchList(block);
    this.dictPoint = dictPoint;
  }

  @Override
  public String toString() {
    return "Block[" +
            "block=" + list + "]";
  }
}
