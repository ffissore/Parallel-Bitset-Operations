package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.io.IOException;

public interface BitSetOperation {

  void compute(OpenBitSetDISI accumulator, DocIdSet bitset) throws IOException;

  OpenBitSetDISI newAccumulator(int bitsetSize);
}
