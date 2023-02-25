package xyz.kumaraswamy.sketchzip.huffman;

import org.junit.jupiter.api.Test;

import java.io.*;
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


    // 11111000101100
    // 11111100 01011001

    // 11111100 01011001
    // 1[111][110][0|0][101][100]1
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
    System.out.println("encoded = " + Arrays.toString(encoded));

    ByteArrayOutputStream decoded = new ByteArrayOutputStream();
    HuffmanDecodeStream decodeStream = new HuffmanDecodeStream(new ByteArrayInputStream(encoded));
    decodeStream.decode();

    int read;
    while ((read = decodeStream.read()) != -1)
      decoded.write(read);
    System.out.println("decoded = " + decoded);
    System.out.println(decoded.toString().equals(text));
  }

  @Test
  public void testTree() throws IOException {
    String text = "abcdawdawA *( SP*(AP*(S(*(*AP*SY(A(SY*P( AS)AU()S() AS**ASdawdef";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    HuffmanEncodeStream stream = new HuffmanEncodeStream(outputStream);

    byte[] bytes = text.getBytes();
    stream.allocate(bytes.length);
    stream.write(bytes);
    stream.encode();

    byte[] encoded = outputStream.toByteArray();
    System.out.println("encoded = " + Arrays.toString(encoded));

    ByteArrayOutputStream decoded = new ByteArrayOutputStream();
    HuffmanDecodeStream decodeStream = new HuffmanDecodeStream(new ByteArrayInputStream(encoded));
    decodeStream.decode();

    int read;
    while ((read = decodeStream.read()) != -1)
      decoded.write(read);
    System.out.println("decoded = " + decoded);
  }

  @Test
  public void encodeFileTest() throws IOException {
    FileInputStream stream = new FileInputStream("/home/kumaraswamy/Documents/melon/sketch-zip/files/hello.txt");
    FileOutputStream outputStream = new FileOutputStream("/home/kumaraswamy/Documents/melon/sketch-zip/files/huffman.txt");

    HuffmanEncodeStream encodeStream = new HuffmanEncodeStream(outputStream);
    encodeStream.allocate(stream.available());
    encodeStream.write(stream.readAllBytes());
    encodeStream.encode();
    outputStream.close();
    stream.close();

    FileInputStream in = new FileInputStream("/home/kumaraswamy/Documents/melon/sketch-zip/files/huffman.txt");
    FileOutputStream os = new FileOutputStream("/home/kumaraswamy/Documents/melon/sketch-zip/files/huffman-decoded.txt");

    HuffmanDecodeStream decodeStream = new HuffmanDecodeStream(in);
    decodeStream.decode();
    os.write(decodeStream.readAllBytes());
    in.close();
    os.close();
  }

  @Test
  public void encodeFileTestFirst() throws IOException {
    String a = "first";
    encode(a);
    encode("second");
  }

  private static void encode(String a) throws IOException {
    FileInputStream stream = new FileInputStream("/home/kumaraswamy/Documents/melon/sketch-zip/files/huffman/" + a + "-part.txt");
    FileOutputStream outputStream = new FileOutputStream("/home/kumaraswamy/Documents/melon/sketch-zip/files/huffman/" + a + "-encoded.txt");

    HuffmanEncodeStream encodeStream = new HuffmanEncodeStream(outputStream);
    encodeStream.allocate(stream.available());
    encodeStream.write(stream.readAllBytes());
    encodeStream.encode();
    outputStream.close();
    stream.close();

  }
}