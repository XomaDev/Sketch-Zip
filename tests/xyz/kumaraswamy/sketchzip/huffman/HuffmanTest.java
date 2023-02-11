package xyz.kumaraswamy.sketchzip.huffman;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

class HuffmanTest {
  @Test
  public void testTreeCreation() throws IOException {
    // 4 + 2 + 2

    // 11111000
    // 11110000000001
    // 11110010 | 11110001


    // 11001110 00110101 101011
    // -50 53 43

    // 00111001
    // 10011100 11

    // 101
    String text = "The Leidenfrost effect is a physical phenomenon in which a liquid, close to a surface that is significantly hotter than the liquid's boiling point, produces an insulating vapor layer that keeps the liquid from boiling rapidly. Because of this repulsive force, a droplet hovers over the surface, rather than making physical contact with it. The effect is named after the German doctor Johann Gottlob Leidenfrost, who described it in A Tract About Some Qualities of Common Water.\n" +
            "\n" +
            "This is most commonly seen when cooking, when drops of water are sprinkled onto a hot pan. If the pan's temperature is at or above the Leidenfrost point, which is approximately 193 °C (379 °F) for water, the water skitters across the pan and takes longer to evaporate than it would take if the water droplets had been sprinkled onto a cooler pan.";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    HuffmanEncodeStream stream = new HuffmanEncodeStream(outputStream);

    byte[] bytes = text.getBytes();
    stream.allocate(bytes.length);
    stream.write(bytes);
    stream.encode();

    byte[] encoded = outputStream.toByteArray();
    System.out.println("encode d= " + Arrays.toString(encoded));

    ByteArrayOutputStream decoded = new ByteArrayOutputStream();
    HuffmanDecodeStream decodeStream = new HuffmanDecodeStream(new ByteArrayInputStream(encoded)) {
      @Override
      public void write(byte b) {
        decoded.write(b);
      }
    };
    decodeStream.decode();
    System.out.println(decoded);
  }
}