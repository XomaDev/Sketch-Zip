package xyz.kumaraswamy.sketchzip.structures;

import java.util.Arrays;

public final class Reference {

  public final Object[] bytes;

  public final int lenDifference;

  public final int onset;
  public final int offset;

  public int frequency;

  public Reference(Object[] bytes, int lenDifference, int onset, int offset) {
    this.bytes = bytes;
    this.lenDifference = lenDifference;
    this.onset = onset;
    this.offset = offset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Reference reference = (Reference) o;
    return Arrays.equals(bytes, reference.bytes);
  }

  @Override
  public int hashCode() {
    int result = onset;
    result = 31 * result + offset;
    return result;
  }

  @Override
  public String toString() {
    return "<" + onset + ':' + offset + '>';
  }


}
