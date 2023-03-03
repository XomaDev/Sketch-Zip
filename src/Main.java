import xyz.kumaraswamy.sketchzip.SketchCode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class Main {

  private static final File FILES = new File(System.getProperty("user.dir"), "/files/");

  public static void main(String[] args) throws IOException {
    // TODO:
    //  handle data in pure binaries
    System.out.println("Hello world!");
    byte[] bytes = Files.readAllBytes(new File(FILES, "hello.txt").toPath());

    ByteArrayOutputStream encodedStream = new ByteArrayOutputStream();
    long initial = System.currentTimeMillis();
    SketchCode.encode(bytes, encodedStream);
    System.out.println("encoded = " + (System.currentTimeMillis() - initial) + " ms");

    byte[] encoded = encodedStream.toByteArray();
    System.out.println("len = " + encoded.length);
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