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
    bs = new DocIdSet[4];
    bs[0] = new SortedVIntList(1, 2, 3);
    bs[1] = new SortedVIntList(2, 3, 4);
    bs[2] = new SortedVIntList(4, 5, 6);
    bs[3] = new SortedVIntList(7, 8, 9);

    threadPool = Executors.newCachedThreadPool();
    bitsetOperationsExecutor = new BitsetOperationsExecutor(threadPool, 1);
  }

  @After
  public void teardown() {
    threadPool.shutdownNow();
  }

  @Test
  public void shouldIntersect() throws Exception {
    Long[] result = bitsetOperationsExecutor.bitsetOperations(bs, new SortedVIntList(2, 3), 10, new IntersectionCount());
    assertEquals(bs.length, result.length);
    assertEquals(2, result[0].longValue());
    assertEquals(2, result[1].longValue());
    assertEquals(0, result[2].longValue());
    assertEquals(0, result[3].longValue());
  }
}
