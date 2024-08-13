package eu.xenit.alfred.api.transaction;

import java.util.concurrent.Callable;

/**
 * Created by jasper on 30/10/17.
 */
public interface ITransactionService {

    /**
     * Execute a transaction, rollbacks if fails.
     *
     * @param func the transaction to execute
     * @param readOnly Whether this is a read only transaction.
     * @param requiresNew <code>true</code> to force a new transaction.
     * @param <T> Return type parameter
     * @return the result of the transaction in case of success.
     */
    <T> T doInTransaction(Callable<T> func, boolean readOnly, boolean requiresNew);
}
