package eu.xenit.apix.util;

public interface SolrTestHelper {

    boolean areTxnsSynced();

    void waitForTxnSync() throws InterruptedException;

    int getFtsStatusCleanDocs();

    boolean isContentIndexed();

    boolean isContentIndexed(int previousCleanCount);

    void waitForContentSync() throws InterruptedException;

    void waitForContentSync(int previousCleanCount) throws InterruptedException;
}
