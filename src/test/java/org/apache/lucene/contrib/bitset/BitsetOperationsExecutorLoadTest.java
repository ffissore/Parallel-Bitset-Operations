package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.lucene.util.SortedVIntList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BitsetOperationsExecutorLoadTest {

  private static final int BS_COUNT = 400000;
  private static final int BS_SIZE = 10000;

  private BitsetOperationsExecutor bitsetOperationsExecutor;
  private DocIdSet[] docIdSets;
  private ExecutorService threadPool;
  private BitSetOperation operation;
  private Random random;

  @Before
  public void setup() {
    System.out.println("==== SETUP");

    random = new Random();

    List<DocIdSet> dids = new LinkedList<DocIdSet>();

    OpenBitSet[] bs = new OpenBitSet[BS_COUNT / 10];
    int howManyBits = random.nextInt(BS_SIZE / 2);
    for (int i = 0; i < bs.length; i++) {
      bs[i] = new OpenBitSet(BS_SIZE);
      for (int b = 0; b < howManyBits; b++) {
        bs[i].fastSet(random.nextInt(BS_SIZE));
      }
    }
    dids.addAll(Arrays.asList(bs));

    SortedVIntList[] svil = new SortedVIntList[BS_COUNT];
    howManyBits = random.nextInt(BS_SIZE / 10);
    for (int i = 0; i < svil.length; i++) {
      int[] ints = new int[howManyBits];
      for (int b = 0; b < howManyBits; b++) {
        ints[b] = random.nextInt(BS_SIZE);
      }
      Arrays.sort(ints);
      svil[i] = new SortedVIntList(ints);
    }
    dids.addAll(Arrays.asList(svil));

    Collections.shuffle(dids);

    docIdSets = dids.toArray(new DocIdSet[dids.size()]);

    threadPool = Executors.newCachedThreadPool();
    bitsetOperationsExecutor = new BitsetOperationsExecutor(threadPool);

    operation = new OR();
  }

  @After
  public void teardown() {
    threadPool.shutdownNow();
  }

  @Test
  public void parrallelShouldBeFaster() throws Exception {
    System.out.println("========= SLOW: START");
    long startAt = System.currentTimeMillis();
    OpenBitSetDISI finalBs = new CallableOperation(docIdSets, 0, docIdSets.length, BS_SIZE, operation).call();
    long slowDuration = System.currentTimeMillis() - startAt;
    System.out.println("========= SLOW: end");

    int randomIndex = random.nextInt(BS_SIZE);

    boolean slowRandomValue = finalBs.get(randomIndex);

    System.out.println("========= FAST: START");
    startAt = System.currentTimeMillis();
    finalBs = bitsetOperationsExecutor.bitsetOperations(docIdSets, BS_SIZE, operation);
    long fastDuration = System.currentTimeMillis() - startAt;
    System.out.println("========= FAST: end");

    boolean fastRandomValue = finalBs.get(randomIndex);

    assertEquals(slowRandomValue, fastRandomValue);

    System.out.println("Slow duration " + slowDuration);
    System.out.println("Fast duration " + fastDuration);

    assertTrue(slowDuration > fastDuration);

    int proc = Runtime.getRuntime().availableProcessors();

    long expectedMaxFastDuration = slowDuration / proc;
    expectedMaxFastDuration += (expectedMaxFastDuration / 100 * 20);
    assertTrue("Was expecting a fast duration less than " + expectedMaxFastDuration, expectedMaxFastDuration > fastDuration);
  }

}
