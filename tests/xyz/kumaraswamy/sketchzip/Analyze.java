package xyz.kumaraswamy.sketchzip;

import xyz.kumaraswamy.sketchzip.huffman.Node;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;

public class Analyze {
  public static void main(String[] args) throws IOException {
    File file = new File("/home/kumaraswamy/Documents/melon/sketch-zip/files/huffman.txt");
    byte[] bytes = Files.readAllBytes(file.toPath());
    BigInteger integer = new BigInteger(bytes);
    System.out.println(integer.toString(2));

    // 11010000111101010010110010110110100001011011111011011001011000000000011
  }
}
