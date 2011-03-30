package org.apache.lucene.contrib.bitset;

import java.io.IOException;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;

public interface BitSetOperation {

  void compute(OpenBitSetDISI accumulator, DocIdSet bitset) throws IOException;
  
}
