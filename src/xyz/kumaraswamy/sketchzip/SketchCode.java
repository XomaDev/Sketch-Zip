package xyz.kumaraswamy.sketchzip;

import xyz.kumaraswamy.sketchzip.decoder.Decoder;
import xyz.kumaraswamy.sketchzip.encoder.Encoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SketchCode {

  public static void encode(byte[] bytes, OutputStream outputStream) throws IOException {
    Encoder encoder = new Encoder(bytes);
    encoder.encode(outputStream);
  }

  public static void decode(InputStream inputStream, OutputStream outputStream) throws IOException {
    new Decoder(inputStream, outputStream);
  }
}
