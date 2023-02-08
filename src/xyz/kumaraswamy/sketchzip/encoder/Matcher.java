package xyz.kumaraswamy.sketchzip.encoder;

import xyz.kumaraswamy.sketchzip.structures.Reference;
import xyz.kumaraswamy.sketchzip.structures.SketchArray;
import xyz.kumaraswamy.sketchzip.structures.SketchList;

import java.util.ArrayList;
import java.util.List;

public class Matcher {

  public static List<Reference> match(SketchList bytes, int minWordSize, int maxWordSize) {
    int len = bytes.length();

    // bytes[] = 'bana{na}'
    // wordSize = 3
    // ban ana nan ana {na*} {a**} ( the last two of total len 6)
    // (wordSize - 1) * 3 == 6
    // allocation = combinations - {unused}

    List<Reference> references = new ArrayList<>();

    int difference = maxWordSize - minWordSize + 1;

    List<SketchArray> words = new ArrayList<>(difference);

    for (int i = 0; i < difference; i++) {
      int wordSize = i + minWordSize;
      int maxAllocation = len * wordSize - (wordSize - 1) * wordSize;
      words.add(new SketchArray(maxAllocation));
    }

    for (int i = 0; i < len; i++) {
      int minOffset = i + minWordSize;

      for (int j = difference - 1, r = maxWordSize;
           j >= 0;
           j--, r--) {
        int offset = i + r;
        if (offset > len)
          continue;
        SketchArray word = words.get(j);

        int wordIndex = word.blockSearch(bytes, r, i);
        if (wordIndex != -1) {
          Object[] aWord = new Object[offset - i];
          for (int k = i, l = 0; k < offset; k++, l++)
            aWord[l] = bytes.get(k);
          Reference ref = Reference.get(aWord, i, offset);
          if (!references.contains(ref))
            references.add(ref);
        } else
          for (int k = i; k < offset; k++)
            word.add(bytes.get(k));
      }

      if (minOffset >= len)
        break;
    }
    return references;
  }
}
