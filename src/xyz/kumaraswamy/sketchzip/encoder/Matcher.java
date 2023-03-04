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

    // TODO:
    //  we must have to improve this technique, it takes a lot
    //  of time to process, we must optimize it

    ArrayList<Reference> usedWords = new ArrayList<>(difference); // maximum allocation

    difference--;
    for (int i = 0, minOffset = minWordSize;
         i < len;
         i++, minOffset++) {
      usedWords.clear();
      // a rotation
      // [kumaraswamy, kumaraswamy]

      for (int j = difference, wSize = maxWordSize;
           j >= 0;
           j--, wSize--) {
        int offset = i + wSize;
        if (offset > len)
          continue;
        SketchArray word = words.get(j);
        int wordIndex = word.blockSearch(bytes, wSize, i);
        if (wordIndex != -1) {
          // TODO:
          //  try to optimize it
          Object[] aWord = new Object[wSize];
          for (int k = i, l = 0; k < offset; k++, l++)
            aWord[l] = bytes.get(k);
          Reference ref = new Reference(aWord, wSize, i, offset);
          if (!references.contains(ref))
            //  we need to first compare frequency of
            //  the larger word with the frequency of
            //  the smaller word, if smaller word freq >
            //  then we should add that first
          {
            int frequency = bytes.frequencyOf(aWord);
            if (frequency > 1) {
              ref.frequency = frequency;
              usedWords.add(ref);
            }
          }
        } else
          for (int k = i; k < offset; k++)
            word.add(bytes.get(k));
      }
      if (!usedWords.isEmpty()) {
        usedWords.sort((o1, o2) -> Integer.compare(
                o2.frequency, o1.frequency));
        references.addAll(usedWords);
      }
      if (minOffset >= len)
        break;
    }
    return references;
  }
}
