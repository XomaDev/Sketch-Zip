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

import static xyz.kumaraswamy.sketchzip.Pencil.HUFFMAN;

public class Encoder {

  private final byte[] bytes;

  private short index = Short.MIN_VALUE;

  private final ByteArrayOutputStream dictionary = new ByteArrayOutputStream();

  private final HuffmanEncodeStream huffmanStream;
  private final BitOutputStream bitsStream;

  public Encoder(byte[] bytes, OutputStream outputStream) throws IOException {
    this.bytes = bytes;

    outputStream.write(HUFFMAN);

    // encoder = > bit stream wrapper = > huffman stream
    huffmanStream = new HuffmanEncodeStream(outputStream);
    bitsStream = new BitOutputStream(huffmanStream);

    encode();
  }

  private void encode() throws IOException {
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
    bitsStream.write(dictionary.toByteArray());

    // actual content
    for (Block block : blocks) {
      for (Object element : block.list)
        if (element instanceof Pointer pointer) {
          bitsStream.write(pointer.rep());

          short index = pointer.index();

          // writes dict index; short; 2 bytes
          bitsStream.write(index >> 8);
          bitsStream.write((byte) index);
        } else bitsStream.write((byte) element);
    }
    // sz[range headers length][dictionary length]{range headers}{dictionary}{content}
    System.out.println("predict output size = " + allocateCap);

    bitsStream.close();
    huffmanStream.encode();
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
        text.append('<')
                .append(pointer.index())
                .append('>');
      else text.append((char) (byte) letter);
    return text.toString();
  }
}
