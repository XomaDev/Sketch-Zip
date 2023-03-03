package xyz.kumaraswamy.sketchzip.encoder;

import xyz.kumaraswamy.sketchzip.Pencil;
import xyz.kumaraswamy.sketchzip.huffman.HuffmanEncodeStream;
import xyz.kumaraswamy.sketchzip.io.BitOutputStream;
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

  private final ByteArrayOutputStream dictionaryBytes = new ByteArrayOutputStream();
  private final BitOutputStream dictionary = new BitOutputStream(dictionaryBytes);

  private final HuffmanEncodeStream huffmanStream;
  private final BitOutputStream bitsStream;

  public Encoder(byte[] bytes, OutputStream outputStream) throws IOException {
    this.bytes = bytes;

    outputStream.write(NAME);

    // encoder = > bit stream wrapper = > huffman stream
    huffmanStream = new HuffmanEncodeStream(outputStream);
    bitsStream = new BitOutputStream(huffmanStream);

    encode();
  }

  private void encode() throws IOException {
    // TODO:
    //  find an optimal way to represent
    //  multiple dictionaries
    // 5hello3Zak
    PointRange range = new PointRange(bytes);
    long n = System.currentTimeMillis();
    List<Block> blocks = range.generateRanges();
    System.out.println("time to generate ranges = " + (System.currentTimeMillis() - n) + "ms");

    // hello hello
    // {hello} {hello}
    // {hello, ello , llo h, lo he, o hel,  hell, hello}


    int gain = encodeBlocks(blocks);

    while (gain > 0)
      gain = encodeBlocks(blocks);

    // 1 byte = dict rep
    // 4 byte = offset;
    dictionary.close();

    int rangeHeadersSize = 5 * blocks.size();
    int dictionarySize = dictionaryBytes.size();

    int contentSize = 0;
    for (Block block : blocks)
      contentSize += block.list.netSize();

    int allocateCap = 4 * 2 + rangeHeadersSize + dictionarySize + contentSize;
    huffmanStream.allocate(allocateCap);

    // sz[range headers length][dictionary length]{range headers}{dictionary}{content}
    // range headers, dict_assigned, each number made of 2 bytes (8 * 2; 16 bit short)
    // [2 byte] + [4 byte] + [4 byte] + [range headers size] + [dictionary size] + [content Size]

    bitsStream.writeInt32(rangeHeadersSize); // 4 bytes
    bitsStream.writeInt32(dictionarySize); // 4 bytes

    // TODO:
    //  we need to separate these length headers

    // range headers
    // <bit><8 byte range>

    int offset = 0;
    for (Block block : blocks) {
      bitsStream.write(block.dictPoint); // 1 byte

      offset += block.list.netSize();
      bitsStream.writeInt32(offset);
    }
    bitsStream.write(dictionaryBytes.toByteArray());

    // actual content
    for (Block block : blocks) {
      for (Object element : block.list)
        if (element instanceof Pointer pointer) {
          bitsStream.write(pointer.rep());

          short index = pointer.index();

          // writes dict index; short; 2 bytes
          bitsStream.writeShort16(index);
        } else bitsStream.write((byte) element);
    }
    // sz[range headers length][dictionary length]{range headers}{dictionary}{content}
    System.out.println("predict output size = " + allocateCap);

    bitsStream.close();
    huffmanStream.encode();
  }

  private int encodeBlocks(List<Block> blocks) throws IOException {

    int netGain = 0, totalSize = 0;
    for (Block block : blocks) {
      SketchList list = block.list;

      int originalSize = list.netSize();
      encodeOccurrences(list, block.dictPoint);

      int compressedSize = list.netSize();

      totalSize += compressedSize;
      netGain += originalSize - compressedSize;
    }

    SketchList net = new SketchList(totalSize);
    for (Block block : blocks)
      for (Object element : block.list)
        net.add(element);
    return netGain;
  }

  private void encodeOccurrences(SketchList list, byte dictPoint) throws IOException {
    List<Reference> matches = Matcher.match(list,
            Pencil.MINIMUM_WORD_SIZE, Pencil.MAXIMUM_WORD_SIZE);

    for (Reference reference : matches) {
      Object[] word = reference.bytes;

      int indexOf = list.findIndexOf(word);
      if (indexOf == -1)
        continue;
      int wordLen = reference.lenDifference;

      writeToDictionary(word, dictPoint);
      if (index == Short.MAX_VALUE)
        throw new RuntimeException("Integer Overflow");

      Pointer pointer = new Pointer(index++, dictPoint);
      int onset = indexOf;

      do {
        list.replace(onset, onset + wordLen, pointer);
      } while ((onset = list.findIndexOf(word)) != -1);
    }
    // [32 bit] dict length, {dictionary; 5;hello}
    // content
  }

  private void writeToDictionary(Object[] word, byte dictPoint) throws IOException {
    short length = 0;

    for (Object letter : word)
      if (letter instanceof Pointer)
        length += 3;
      else length++;

    if (length > 256)
      throw new IOException("Exceeded maximum dictionary capacity");
    dictionary.write(length); // this cannot be 0
    for (Object letter : word)
      if (letter instanceof Pointer pointer) {
        // total of 3 bytes

        dictionary.write(dictPoint);

        short index = pointer.index();
        // 16 bit index
        dictionary.writeShort16(index);
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
        text.append('<')
                .append(pointer.index())
                .append('>');
      else text.append((char) (byte) letter);
    return text.toString();
  }
}
