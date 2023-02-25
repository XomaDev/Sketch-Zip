package xyz.kumaraswamy.sketchzip.structures;

import xyz.kumaraswamy.sketchzip.huffman.Node;

import java.util.Arrays;

public class FrequencyQueue {

  private final Node[] elements;

  private final int capacity;
  private int index = 0;

  public FrequencyQueue(int len) {
    capacity = len;
    elements = new Node[len];
  }

  public void add(Node elm) {
    // for us, the capacity never increases,
    // but rather decreases
    elements[index++] = elm;
    shift(elm);
  }

  // merges all the trees until
  // only one single tree is left
  public Node poll() {
    while (index != 1)
      merge();
    return elements[0];
  }

  private void merge() {
    Node first = elements[0];
    Node second = elements[1];

    for (int i = 0; i + 2 < capacity; i++)
      elements[i] = elements[i + 2];
    index -= 2;

    // for more readability
    elements[capacity - 1] = null;
    elements[capacity - 2] = null;

    add(new Node(first, second));
  }

  private void shift(Node element) {
    int last = index - 1;

    while (--last >= 0) {
      Node before = elements[last];
      if (before.freq > element.freq) {
        Node next = elements[last + 1];
        elements[last + 1] = elements[last];
        elements[last] = next;
      } else {
        break;
      }
    }
  }

  @Override
  public String toString() {
    return Arrays.toString(elements);
  }
}