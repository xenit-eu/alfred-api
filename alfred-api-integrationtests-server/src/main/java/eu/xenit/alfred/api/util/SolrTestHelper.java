package eu.xenit.alfred.api.util;


public interface SolrTestHelper {

    boolean areTransactionsSynced();

    void waitForTransactionSync() throws InterruptedException;

    int getNumberOfFtsStatusCleanDocs();

    boolean isContentIndexed();

    boolean isContentIndexed(int previousCleanCount);

    void waitForContentSync() throws InterruptedException;

    void waitForContentSync(int previousCleanCount) throws InterruptedException;
}
