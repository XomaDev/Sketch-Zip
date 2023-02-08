package xyz.kumaraswamy.sketchzip.structures;

import java.util.Arrays;

public class SketchArray {

  private final int maxLength;

  private int size = 20, index = 0;

  private Object[] bytes;

  public SketchArray(int maxLength) {
    this.maxLength = maxLength;
    bytes = new Object[size];
  }

  public void add(Object b) {
    if (index == size)
      resize();
    bytes[index++] = b;
  }

  private void resize() {
    int newSize = size * 2;
    if (newSize > maxLength)
      newSize = maxLength;
    Object[] allocation = new Object[newSize];
    if (size >= 0)
      System.arraycopy(bytes, 0, allocation, 0, size);
    bytes = allocation;
    size = newSize;
  }

  public int blockSearch(SketchList reference, int wordSize, int onset) {
    search:
    for (int i = 0; i < size; i += wordSize) {
      for (int j = i, k = 0, l = onset;
           j < i + wordSize;
           j++, k++, l++) {
        if (j == size)
          return -1;
        if (!equals(bytes[j], reference.get(l)))
          continue search;
      }
      return i;
    }
    return -1;
  }

  private boolean equals(Object object, Object another) {
    if (object == null && another == null)
      return true;
    if (object != null)
      return object.equals(another);
    return false;
  }

  @Override
  public String toString() {
    return '[' + Arrays.toString(bytes) + ']';
  }
}
