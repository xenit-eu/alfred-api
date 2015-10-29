package eu.xenit.apix.alfresco.transaction;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.transaction.ITransactionService;
import java.util.concurrent.Callable;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@OsgiService
@Component("eu.xenit.apix.alfresco.transaction.TransactionService")
public class TransactionService implements ITransactionService {

    @Autowired
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