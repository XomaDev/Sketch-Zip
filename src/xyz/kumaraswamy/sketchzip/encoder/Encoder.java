package xyz.kumaraswamy.sketchzip.encoder;

import xyz.kumaraswamy.sketchzip.Pencil;
import xyz.kumaraswamy.sketchzip.huffman.HuffmanEncodeStream;
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
  private HuffmanEncodeStream huffmanStream;

  public Encoder(byte[] bytes) {
    this.bytes = bytes;
  }

  public void encode(OutputStream outputStream) throws IOException {
    outputStream.write(NAME);
    encode(new HuffmanEncodeStream(outputStream));
  }

  public void encode(HuffmanEncodeStream huffmanStream) throws IOException {
    this.huffmanStream = huffmanStream;

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
    int dictionarySize = dictionary.size();

    int contentSize = 0;
    for (Block block : blocks)
      contentSize += block.list.netSize();

    int allocateCap = 4 * 2 + rangeHeadersSize + dictionarySize + contentSize;

    huffmanStream.allocate(allocateCap);

    // sz[range headers length][dictionary length]{range headers}{dictionary}{content}
    // range headers, dict_assigned, each number made of 2 bytes (8 * 2; 16 bit short)
    // [2 byte] + [4 byte] + [4 byte] + [range headers size] + [dictionary size] + [content Size]

    writeInt(rangeHeadersSize); // 4 bytes
    writeInt(dictionarySize); // 4 bytes

    // TODO:
    //  we need to separate these length headers

    // range headers
    // <bit><8 byte range>

    int offset = 0;
    for (Block block : blocks) {
      huffmanStream.write(block.dictPoint); // 1 byte

      offset += block.list.netSize();
      writeInt(offset); // 4 byte
    }
    huffmanStream.write(dictionary.toByteArray());

    // actual content
    for (Block block : blocks) {
      for (Object element : block.list)
        if (element instanceof Pointer pointer) {
          huffmanStream.write(pointer.rep());

          short index = pointer.index();

          // writes dict index; short; 2 bytes
          huffmanStream.write(index >> 8);
          huffmanStream.write((byte) index);
        } else huffmanStream.write((byte) element);
    }
    // sz[range headers length][dictionary length]{range headers}{dictionary}{content}
    System.out.println("predict output size = " + allocateCap);
    huffmanStream.encode();
  }

  private void writeInt(int n) {
    huffmanStream.write(n >> 24);
    huffmanStream.write(n >> 16);
    huffmanStream.write(n >> 8);
    huffmanStream.write(n);
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
