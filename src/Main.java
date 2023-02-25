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
    long initial = System.currentTimeMillis();
    byte[] bytes = Files.readAllBytes(new File(FILES, "hello.txt").toPath());

    ByteArrayOutputStream encodedStream = new ByteArrayOutputStream();
    SketchCode.encode(bytes, encodedStream);

    byte[] encoded = encodedStream.toByteArray();
    System.out.println("len = " + encoded.length);
    System.out.println("encoded = " + (System.currentTimeMillis() - initial) + " ms");
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