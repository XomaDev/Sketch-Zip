# Sketch-Zip

The zipping algorithm combines LZW and Huffman coding to compress data efficiently, using an optimal dictionary word
size that can vary from 5 to 10 for encoding. It can handle a maximum input data length of `2^32`.

The LZW algorithm generates a dictionary of frequently occurring words from the input data. This dictionary is used to
encode the input data, which can be divided into dynamically sized blocks. Each block is compressed using LZW and
encoded using the dictionary. The resulting encoded data is further compressed using Huffman coding.

The decoder first decompresses the Huffman-coded data using a Huffman tree and then decodes it using the compressed
dictionary. The decoded data is then decompressed using LZW to recover the original input data.

### About

`Started` January 20<sup>th</sup>, 2023
<br>
`Finished` February 17<sup>th</sup> 2023

Ah, a lot of things during those days, I wish I was able to finish them earlier, exams got in my way.

The performance of Sketch Zip can be influenced by various factors such as the type of data being compressed, the
frequency and length of repeated sequences, and the amount of computational power available. While speed wasn't my
primary concern when creating the program. (Sadly, its too slow)

### Compression Ratio

The table shows the compression ratios achieved by three different algorithms for an input data size of 19.6 kB.

| Algorithm            | Input Size | Output Size | Compression Ratio |
|----------------------|------------|-------------|-------------------|
| Sketch Zip Algorithm | 25.5 kb    | 13.1 kB     | 48.2%             |
| 7zip Algorithm       | 25.5 kb    | 7.3 KB      | 71.4%             |
| Xz Algorithm         | 25.5 kb    | 7.3 kB      | 71.4%             |

(miaw, there are a lot of things that can be improved)

### Using

Not sure why would anyone use this, I just created it to learn, anyways.

**Encoding**

````java
// bytes are the input, and the second argument
// is the stream, where the encoded output is written
SketchCode.encode(bytes,outputStream);
````

**Decoding**

````java
// decode(encodedStream, decodedStream)
// first argument -> provide the encoded data
// second argument -> output stream where you will receive the original data
SketchCode.decode(new ByteArrayInputStream(encoded),decodedStream);
````

Kumaraswamy B.G
