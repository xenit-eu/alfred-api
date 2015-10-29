package eu.xenit.apix.alfresco.workflow.activiti.query;

import eu.xenit.apix.workflow.search.IQueryFilter;
import eu.xenit.apix.workflow.search.Sorting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.query.Query;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ApixHistoricInstanceQuery {

    private static final QName historyServiceQName = QName.createQName((String) null, "activitiHistoryService");
    private static final QName taskServiceQName = QName.createQName((String) null, "activitiTaskService");
    private final Logger logger = LoggerFactory.getLogger(ApixHistoricInstanceQuery.class);
    protected ServiceRegistry serviceRegistry;
    private HistoryService historyService;
    private NamespaceService namespaceService;
    private PersonService personService;
    private AuthorityService authorityService;
    private TaskService taskService;
    private AuthenticationService authenticationService;

    public ApixHistoricInstanceQuery(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        init();
    }

    protected void init() {
        this.historyService = (HistoryService) this.serviceRegistry.getService(historyServiceQName);
        if (this.historyService == null) {
            throw new Error("HistoryService not found!");
        } else {
            this.namespaceService = this.serviceRegistry.getNamespaceService();
            this.personService = this.serviceRegistry.getPersonService();
            this.authorityService = this.serviceRegistry.getAuthorityService();
            this.taskService = (TaskService) this.serviceRegistry.getService(taskServiceQName);
            this.authenticationService = this.serviceRegistry.getAuthenticationService();
        }
    }

    public abstract void setActive(boolean var1);

    public abstract void orderBy(List<Sorting> var1);

    public abstract void filter(List<IQueryFilter> var1);

    public abstract <T> List<T> getAll();

    protected HistoryService getHistoryService() {
        return this.historyService;
    }

    protected NamespaceService getNamespaceService() {
        return this.namespaceService;
    }

    protected PersonService getPersonService() {
        return this.personService;
    }

    protected AuthorityService getAuthorityService() {
        return this.authorityService;
    }

    protected TaskService getTaskService() {
        return this.taskService;
    }

    protected AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }

    protected ScriptNode getUserScriptNode(String userName) {
        NodeRef personNodeRef = this.getPersonService().getPersonOrNull(userName);
        if (personNodeRef == null) {
            this.logger.error("User with name '" + userName + "' was not found!");
            return null;
        } else {
            return new ActivitiScriptNode(personNodeRef, this.serviceRegistry);
        }
    }

    protected String getShortQName(String property) {
        QName qname = QName.createQName(property);
        String activitiQNameSuffix = qname.getLocalName() == null ? "" : qname.getLocalName();
        String namespaceUri = qname.getNamespaceURI();
        if (namespaceUri != null && !qname.getNamespaceURI().equals("")) {
            if (!namespaceUri.equals(activitiQNameSuffix)) {
                this.logger.error("namespaceUri: " + namespaceUri);
                Collection<String> availableNamespacePrefixes = this.getNamespaceService().getPrefixes(namespaceUri);
                if (availableNamespacePrefixes.size() > 0) {
                    List<String> availableNamespacePrefixesList = new ArrayList(availableNamespacePrefixes);
                    String shortQNamePrefix = (String) availableNamespacePrefixesList.get(0);
                    this.logger.error("shortQNamePrefix: " + shortQNamePrefix);
                    String activitiQName =
                            shortQNamePrefix != null && !shortQNamePrefix.isEmpty() ? shortQNamePrefix + "_"
                                    + activitiQNameSuffix : activitiQNameSuffix;
                    this.logger.error("Generated short QName '" + activitiQName + "' based on the provided QName: '"
                            + property + "'");
                    return activitiQName;
                }

                this.logger.error("availableNamespacePrefixes is empty");
            } else {
                this.logger.error("namespaceUri equals activitiQNameSuffix");
            }
        } else {
            this.logger.error("namespaceUri is null or empty");
        }

        return null;
    }

    protected void setOrdering(Query query, String order) {
        if (order.equals(Sorting.ASCENDING)) {
            query.asc();
        } else {
            query.desc();
        }
    }
}
