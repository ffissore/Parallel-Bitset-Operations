package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.SortedVIntList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class IntersectionCountTest {

  private DocIdSet[] bs;
  private ExecutorService threadPool;
  private BitsetOperationsExecutor bitsetOperationsExecutor;

  @Before
  public void setup() {
    bs = new DocIdSet[3];
    bs[0] = new SortedVIntList(new int[]{1, 2, 3});
    bs[1] = new SortedVIntList(new int[]{2, 3, 4});
    bs[2] = new SortedVIntList(new int[]{4, 5, 6});

    threadPool = Executors.newCachedThreadPool();
    bitsetOperationsExecutor = new BitsetOperationsExecutor(threadPool);
  }

  @After
  public void teardown() {
    threadPool.shutdownNow();
  }


  @Test
  public void shouldIntersect() throws Exception {
    int finalBitSetSize = 4;
    long[] result = bitsetOperationsExecutor.bitsetOperations(bs, new SortedVIntList(new int[]{2, 3}), finalBitSetSize, new IntersectionCount());
    assertEquals(2, result[0]);
    assertEquals(2, result[1]);
    assertEquals(0, result[2]);
  }
}
