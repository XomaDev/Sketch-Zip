package xyz.kumaraswamy.sketchzip;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SketchCodeTest {

  @Test
  public void test() throws IOException {
    String text = "kumaraswamy kumaraswamy";

    byte[] bytes = text.getBytes();

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    SketchCode.encode(bytes, stream);

    System.out.println("encoded = " + stream);
    System.out.println(Arrays.toString(stream.toByteArray()));

    ByteArrayOutputStream decodedStream = new ByteArrayOutputStream();
    SketchCode.decode(new ByteArrayInputStream(stream.toByteArray()), decodedStream);

    System.out.println("decoded = " + decodedStream);
  }

  @Test
  public void testSecond() throws IOException {
    ByteArrayOutputStream input = new ByteArrayOutputStream();

    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++)
      input.write((byte) i);
    input.write(Byte.MAX_VALUE);

    byte[] bytes = input.toByteArray();
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    SketchCode.encode(bytes, stream);

    System.out.println("encoded = " + stream);
    System.out.println(Arrays.toString(stream.toByteArray()));

    ByteArrayOutputStream decodedStream = new ByteArrayOutputStream();
    SketchCode.decode(new ByteArrayInputStream(stream.toByteArray()), decodedStream);

    System.out.println("decoded = " + decodedStream);
    System.out.println(Arrays.toString(decodedStream.toByteArray()));
    Assertions.assertArrayEquals(bytes, decodedStream.toByteArray());
  }

  @Test
  public void testBehaviour() {
    List<int[]> list = new ArrayList<>();
    list.add(new int[] {1, 2});
    System.out.println(list.contains(new int[] {1, 2}));
  }
}