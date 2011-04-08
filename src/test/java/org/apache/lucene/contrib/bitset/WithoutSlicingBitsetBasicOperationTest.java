package org.apache.lucene.contrib.bitset;

import org.junit.Before;

public class WithoutSlicingBitsetBasicOperationTest extends AbstractBitsetBasicOperationsTest {

  @Before
  public void setup() {
    super.setup();
    bitsetOperationsExecutor = new BitsetOperationsExecutor(threadPool, 10000);
  }

}
