package xyz.kumaraswamy.sketchzip.structures;

import java.util.Arrays;
import java.util.WeakHashMap;

public class Reference {

  private static final WeakHashMap<Integer, Reference> weakerReferences = new WeakHashMap<>();

  public static Reference get(Object[] bytes, int onset, int offset) {
    int sum = onset + offset;
    Reference weakRef = weakerReferences.get(sum);
    if (weakRef != null)
      return weakRef;
    weakRef = new Reference(bytes, onset, offset);
    weakerReferences.put(sum, weakRef);
    return weakRef;
  }

  public final Object[] bytes;

  public final int onset;
  public final int offset;

  public Reference(Object[] bytes, int onset, int offset) {
    this.bytes = bytes;
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
