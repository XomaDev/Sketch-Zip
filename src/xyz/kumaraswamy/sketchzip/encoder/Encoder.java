package xyz.kumaraswamy.sketchzip.encoder;

import xyz.kumaraswamy.sketchzip.Pencil;
import xyz.kumaraswamy.sketchzip.structures.Block;
import xyz.kumaraswamy.sketchzip.structures.Pointer;
import xyz.kumaraswamy.sketchzip.structures.Reference;
import xyz.kumaraswamy.sketchzip.structures.SketchList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static xyz.kumaraswamy.sketchzip.Pencil.NAME;

public class Encoder {

  private final byte[] bytes;

  private short index = Short.MIN_VALUE;

  private final ByteArrayOutputStream dictionary = new ByteArrayOutputStream();
  private OutputStream outputStream;

  public Encoder(byte[] bytes) {
    this.bytes = bytes;
  }

  public void encode(OutputStream resultStream) throws IOException {
    // TODO:
    //  we need to calculate the final
    //  output size, so that we can pass it to a huffman coder
    this.outputStream = resultStream;

    PointRange range = new PointRange(bytes);
    List<Block> blocks = range.generateRanges();

    // hello hello
    // {hello} {hello}
    // {hello, ello , llo h, lo he, o hel,  hell, hello}


    int gain = encodeBlocks(blocks);

    while (gain > 0)
      gain = encodeBlocks(blocks);

    // 1 byte = dict rep
    // 4 byte = offset;
    int rangeHeadersSize = 5 * blocks.size();

    // sz[range headers length][dictionary length]{range headers}{dictionary}{content}
    // range headers, dict_assigned, each number made of 2 bytes (8 * 2; 16 bit short)
    // [2 byte] + [4 byte] + [4 byte] + [range headers size] + [dictionary size] + [content Size]

    resultStream.write(NAME);

    writeInt(rangeHeadersSize); // 4 bytes
    writeInt(dictionary.size()); // 4 bytes

    // TODO:
    //  we need to separate these length headers

    // range headers
    // <bit><8 byte range>

    int offset = 0;
    for (Block block : blocks) {
      resultStream.write(block.dictPoint); // 1 byte

      offset += block.list.netSize();
      writeInt(offset); // 4 byte
    }
    resultStream.write(dictionary.toByteArray());

    // actual content
    for (Block block : blocks)
      for (Object element : block.list)
        if (element instanceof Pointer pointer) {
          resultStream.write(pointer.rep());

          short index = pointer.index();

          // writes dict index; short; 2 bytes
          resultStream.write(index >> 8);
          resultStream.write((byte) index);
        } else resultStream.write((byte) element);
  }

  private void writeInt(int n) throws IOException {
    outputStream.write(n >> 24);
    outputStream.write(n >> 16);
    outputStream.write(n >> 8);
    outputStream.write(n);
  }

  private int encodeBlocks(List<Block> blocks) {
    SketchList net = new SketchList();

    int netGain = 0;
    for (Block block : blocks) {
      SketchList list = block.list;
      netGain += encodeOccurrences(list, block.dictPoint);
      for (Object element : list)
        net.add(element);
    }
    return netGain;
  }

  private int encodeOccurrences(SketchList list, byte dictPoint) {
    int originalSize = list.netSize();

    List<Reference> matches = Matcher.match(list,
            Pencil.MINIMUM_WORD_SIZE, Pencil.MAXIMUM_WORD_SIZE);

    for (Reference reference : matches) {
      Object[] word = reference.bytes();

      int indexOf = list.findIndexOf(word);
      if (indexOf != -1) {
        int wordLen = reference.offset() - reference.onset();

        writeToDictionary(word, dictPoint);

        if (index == Short.MAX_VALUE)
          throw new RuntimeException("Integer Overflow");
        Pointer pointer = new Pointer(index++, dictPoint);
        int onset;
        while ((onset = list.findIndexOf(word)) != -1)
          list.replace(onset, onset + wordLen, pointer);
      }
    }
    System.out.println("new: " + list.netSize());
    // [32 bit] dict length, {dictionary; 5;hello}
    // content
    return originalSize - list.netSize();
  }

  private void writeToDictionary(Object[] word, byte dictPoint) {
    int length = 0;

    for (Object letter : word)
      if (letter instanceof Pointer)
        length += 3;
      else length++;
    dictionary.write(length);
    for (Object letter : word)
      if (letter instanceof Pointer pointer) {
        // total of 3 bytes

        dictionary.write(dictPoint);

        short index = pointer.index();

        // writes dict index; short; 2 bytes
        dictionary.write(index >> 8);
        dictionary.write((byte) index);
      } else dictionary.write((byte) letter);
  }

  @SuppressWarnings("unused")
  public static String printable(Object[] word) {
    // for debugging
    StringBuilder text = new StringBuilder();
    for (Object letter : word)
      if (letter == null)
        break;
      else if (letter instanceof Pointer pointer)
        text.append("<")
                .append(pointer.index())
                .append('>');
      else text.append((char) (byte) letter);
    return text.toString();
  }
}
