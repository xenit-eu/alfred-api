package eu.xenit.alfred.api.alfresco.workflow;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.workflow.model.ITaskOrWorkflow;
import eu.xenit.alfred.api.workflow.model.WorkflowOrTaskChanges;
import java.io.Serializable;
import java.util.Map;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractApixWorkflowConvertor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractApixWorkflowConvertor.class);

    @Autowired
    private IPeopleService peopleService;

    @Autowired
    private AuthorityService authorityService;

    protected static void PutOnMapIfNotNull(Map<String, Serializable> map, String property, Serializable value) {
        if (value != null) {
            map.put(property, value);
        }
    }

    protected static void PutOnMapIfNotNull(Map<String, Serializable> map, QName property, Serializable value) {
        if (value != null) {
            map.put(property.toString(), value);
        }
    }

    public abstract <T> String getId(T var1);

    public abstract ITaskOrWorkflow apply(String var1);

    public abstract <T> ITaskOrWorkflow apply(T var1);

    public abstract void update(String var1, WorkflowOrTaskChanges var2);

    public abstract void generate(int var1, String var2);

    public abstract void end(String var1, String var2);

    public abstract void claim(String var1);

    public abstract void claim(String var1, String var2);

    public abstract void release(String var1);

    protected String getCurrentUser() {
        return this.peopleService.GetCurrentUser().getUserName();
    }

    protected NodeRef getCurrentUserNodeRef() {
        return this.peopleService.GetCurrentUser().getNodeRef();
    }

    protected NodeRef getUserNodeRef(String assigneeName) {
        return this.peopleService.GetPerson(assigneeName).getNodeRef();
    }

    protected boolean isAdmin(String userName) {
        return authorityService.isAdminAuthority(userName);
    }
}
