package eu.xenit.alfred.api.alfresco.transaction;

import eu.xenit.alfred.api.transaction.ITransactionService;
import java.util.concurrent.Callable;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("eu.xenit.alfred.api.alfresco.transaction.TransactionService")
public class TransactionService implements ITransactionService {

    @Autowired
    @Qualifier("ServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Override
    public <T> T doInTransaction(Callable<T> func, boolean readOnly, boolean requiresNew) {
        final Callable<T> funcFinal = func;
        RetryingTransactionHelper.RetryingTransactionCallback callback = new RetryingTransactionHelper.RetryingTransactionCallback() {
            @Override
            public Object execute() throws Throwable {
                return funcFinal.call();
            }
        };
        return (T) serviceRegistry.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(callback, readOnly, requiresNew);
    }
}