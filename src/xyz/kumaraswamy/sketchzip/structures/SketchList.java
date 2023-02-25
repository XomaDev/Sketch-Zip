package xyz.kumaraswamy.sketchzip.structures;

import java.util.Iterator;

public class SketchList implements Iterable<Object> {

  private int length;

  public Object[] elements;

  private int index = 0;

  public SketchList() {
    length = 500;
    elements = new Object[length];
  }

  public SketchList(byte[] bytes) {
    length = bytes.length;
    elements = new Object[length];
    for (int i = 0; i < length; i++)
      elements[i] = bytes[i];
  }

  public void add(Object object) {
    if (index == length)
      resize();
    elements[index++] = object;
  }

  private void resize() {
    int newCap = length * 2;
    Object[] allocation = new Object[newCap];
    if (index >= 0)
      System.arraycopy(elements, 0, allocation, 0, index);
    this.length = newCap;
    this.elements = allocation;
  }

  public int length() {
    return length;
  }

  public int netSize() {
    int netSize = 0;
    for (Object element : this)
      if (element instanceof Pointer)
        // 1 byte pointer + 2 byte ref
        netSize += 3;
      else netSize++;
    return netSize;
  }

  public Object get(int index) {
    return elements[index];
  }

  @Override
  public Iterator<Object> iterator() {
    return new Iterator<>() {

      private int itrIndex = 0;

      @Override
      public boolean hasNext() {
        return itrIndex < length;
      }

      @Override
      public Object next() {
        return elements[itrIndex++];
      }
    };
  }

  public int findIndexOf(Object[] sequence) {
    return findIndexOf(sequence, 0);
  }

  public int findIndexOf(Object[] sequence, int from) {
    int slen = sequence.length;
    search:
    for (int i = from; i < length; i++)
      if (elements[i].equals(sequence[0])) {
        for (int j = i + 1, k = 1;
             j < i + slen; // offset [current index] + [sequence length]
             j++, k++) {
          if (!(j < length)) return -1;
          else if (!elements[j].equals(sequence[k]))
            continue search;
        }
        return i;
      }
    return -1;
  }

  public int frequencyOf(Object... elements) {
    int wordLen = elements.length;
    int freq = 0, onset = 0;
    while ((onset = findIndexOf(elements, onset)) != -1) {
      freq++;
      onset += wordLen;
    }
    return freq;
  }

  public void replace(int onset, int offset, Object... with) {
    int wlen = with.length;
    int diffOff = offset - onset;
    int newCap = length - diffOff + wlen;

    Object[] newAlloc = new Object[newCap];

    // { 1, 2, 3, 4, 5 };
    // onset = 2
    // offset = 4

    for (int i = 0, j = 0; // i for older E[], j for newer E[]
         j < newCap;
         i++, j++)
      if (i == onset) {
        for (int k = 0; k < wlen; j++, k++)
          newAlloc[j] = with[k];
        j--; // -1 because j++ will be done
        i += diffOff - 1; // i + replace_len and
        // -1 because i++ will be done
      } else newAlloc[j] = elements[i];

    this.length = newCap;
    this.elements = newAlloc;
  }

  @Override
  public String toString() {
    return getString(0, length);
  }

  public String getString(int onset, int offset) {
    StringBuilder text = new StringBuilder();

    for (int i = onset; i < offset; i++) {
      text.append(elements[i]);
      if (i + 1 != offset)
        text.append(", ");
    }
    return "SketchList{" +
            "wrapper=" + text +
            '}';
  }
}
