package xyz.kumaraswamy.sketchzip.structures;

public record Pointer(short index, byte rep) {

  @Override
  public String toString() {
    return "P(" + index + ':' + rep + ')';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Pointer pointer = (Pointer) o;

    return index == pointer.index;
  }
}
