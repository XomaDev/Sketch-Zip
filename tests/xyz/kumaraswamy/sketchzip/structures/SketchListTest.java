package xyz.kumaraswamy.sketchzip.structures;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class SketchListTest {
  @Test
  public void frequency() {
    String text = "kumarswamy kumar kumarswamy";
    byte[] bytes = text.getBytes();
    SketchList list = new SketchList(bytes);
    System.out.println(Arrays.toString(bytes));
    int freq = list.frequencyOf((byte) 107, (byte) 117, (byte) 109, (byte) 97,
            (byte) 114, (byte) 115, (byte) 119, (byte) 97, (byte) 109,
            (byte) 121);
    System.out.println("freq = " + freq);
  }
}