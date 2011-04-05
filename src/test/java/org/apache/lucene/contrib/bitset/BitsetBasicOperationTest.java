package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.lucene.util.SortedVIntList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BitsetBasicOperationTest {

  private DocIdSet[] dis;
  private ExecutorService threadPool;
  private BitsetOperationsExecutor bitsetOperationsExecutor;

  @Before
  public void setup() {
    dis = new DocIdSet[4];

    OpenBitSet bs = new OpenBitSet(10);
    bs.fastSet(1);
    bs.fastSet(5);
    dis[0] = bs;
    bs = new OpenBitSet(10);
    bs.fastSet(0);
    bs.fastSet(1);
    bs.fastSet(2);
    dis[1] = bs;

    dis[2] = new SortedVIntList(1, 4);
    dis[3] = new SortedVIntList(1, 2, 6);

    threadPool = Executors.newCachedThreadPool();
    bitsetOperationsExecutor = new BitsetOperationsExecutor(threadPool, 1);
  }

  @After
  public void teardown() {
    threadPool.shutdownNow();
  }

  @Test
  public void shouldORTheDodIdSets() throws Exception {
    OpenBitSetDISI bs = bitsetOperationsExecutor.bitsetOperations(dis, 10, new OR());
    assertTrue(bs.get(0));
    assertTrue(bs.get(1));
    assertTrue(bs.get(2));
    assertFalse(bs.get(3));
    assertTrue(bs.get(4));
    assertTrue(bs.get(5));
    assertTrue(bs.get(6));
    assertFalse(bs.get(7));
    assertFalse(bs.get(8));
    assertFalse(bs.get(9));
  }

  @Test
  public void shouldANDTheDodIdSets() throws Exception {
    OpenBitSetDISI bs = bitsetOperationsExecutor.bitsetOperations(dis, 10, new AND());
    assertFalse(bs.get(0));
    assertTrue(bs.get(1));
    assertFalse(bs.get(2));
    assertFalse(bs.get(3));
    assertFalse(bs.get(4));
    assertFalse(bs.get(5));
    assertFalse(bs.get(6));
    assertFalse(bs.get(7));
    assertFalse(bs.get(8));
    assertFalse(bs.get(9));
  }

  @Test
  public void shouldNOTTheDodIdSets() throws Exception {
    OpenBitSetDISI bs = bitsetOperationsExecutor.bitsetOperations(dis, 10, new NOT());
    assertFalse(bs.get(0));
    assertFalse(bs.get(1));
    assertFalse(bs.get(2));
    assertFalse(bs.get(3));
    assertFalse(bs.get(4));
    assertTrue(bs.get(5));
    assertFalse(bs.get(6));
    assertFalse(bs.get(7));
    assertFalse(bs.get(8));
    assertFalse(bs.get(9));
  }

  @Test
  public void shouldXORTheDodIdSets() throws Exception {
    OpenBitSetDISI bs = bitsetOperationsExecutor.bitsetOperations(dis, 10, new XOR());
    assertTrue(bs.get(0));
    assertFalse(bs.get(1));
    assertFalse(bs.get(2));
    assertFalse(bs.get(3));
    assertTrue(bs.get(4));
    assertTrue(bs.get(5));
    assertTrue(bs.get(6));
    assertFalse(bs.get(7));
    assertFalse(bs.get(8));
    assertFalse(bs.get(9));
  }
}
