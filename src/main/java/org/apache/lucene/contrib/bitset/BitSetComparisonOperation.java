package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.io.IOException;

public interface BitSetComparisonOperation<T> {

  T compute(OpenBitSetDISI accumulator, DocIdSet target, OpenBitSet toCompare) throws IOException;

}
