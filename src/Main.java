import xyz.kumaraswamy.sketchzip.SketchCode;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

public class Main {

  private static final File FILES = new File(System.getProperty("user.dir"), "/files/");

  public static void main(String[] args) throws IOException {
    System.out.println("Hello world!");
    byte[] bytes = Files.readAllBytes(new File(FILES, "hello.txt").toPath());

    ByteArrayOutputStream encodedStream = new ByteArrayOutputStream();
    SketchCode.encode(bytes, encodedStream);

    byte[] encoded = encodedStream.toByteArray();
    try (FileOutputStream fos =
                 new FileOutputStream(new File(FILES, "encoded.txt"))) {
      fos.write(encoded);
    }

    ByteArrayOutputStream decodedStream = new ByteArrayOutputStream();
    SketchCode.decode(new ByteArrayInputStream(encoded), decodedStream);

    byte[] decoded = decodedStream.toByteArray();
    try (FileOutputStream fos =
                 new FileOutputStream(new File(FILES, "decoded.txt"))) {
      fos.write(decoded);
    }
    assert Arrays.equals(encoded, decoded);
  }
}