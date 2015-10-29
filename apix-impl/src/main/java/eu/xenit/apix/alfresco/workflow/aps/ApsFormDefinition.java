package eu.xenit.apix.alfresco.workflow.aps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

/**
 * POJO class similar to com.activiti.model.editor.form.FormDefinitionRepresentation
 */
@JsonInclude(Include.NON_NULL)
public class ApsFormDefinition {

    protected Long id;
    protected String name;
    protected String processDefinitionId;
    protected String processDefinitionName;
    protected String processDefinitionKey;
    protected String taskId;
    protected String taskName;
    protected String taskDefinitionKey;
    protected List<ApsFormField> fields;
    protected String className;
    protected Map<String, String> metadata = new HashMap<>();
    protected String globalDateFormat;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    /**
     * Do not use this method for logical operations since it only return the top level fields. I.e. A "container"
     * field's sub fields are not returned. For verifying and listing all fields from a form use instead
     * listAllFields().
     *
     * @return The top level fields, a container's sub fields are not returned.
     */
    public List<ApsFormField> getFields() {
        return fields;
    }

    public void setFields(List<ApsFormField> fields) {
        this.fields = fields;
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getGlobalDateFormat() {
        return globalDateFormat;
    }

    public void setGlobalDateFormat(String globalDateFormat) {
        this.globalDateFormat = globalDateFormat;
    }

    /*
     * Helper methods
     */
    public Map<String, ApsFormField> allFieldsAsMap() {
        Map<String, ApsFormField> result = new HashMap<>();
        List<ApsFormField> allFields = listAllFields();
        if (allFields != null) {
            for (ApsFormField field : allFields) {
                if (!result.containsKey(field.getId()) || !("readonly".equals(field.getType()) ||
                        "readonly-text".equals(field.getType()))) {

                    result.put(field.getId(), field);
                }
            }
        }
        return result;
    }

    public List<ApsFormField> listAllFields() {
        List<ApsFormField> listOfAllFields = new ArrayList<>();
        collectSubFields(fields, listOfAllFields);
        return listOfAllFields;
    }

    protected void collectSubFields(List<ApsFormField> fields,
            List<ApsFormField> listOfAllFields) {
        if (CollectionUtils.isNotEmpty(fields)) {
            for (ApsFormField field : fields) {
                listOfAllFields.add(field);
                if (field instanceof ApsContainer) {
                    ApsContainer container = (ApsContainer) field;
                    Map<String, List<ApsFormField>> subFieldMap = container.getFields();
                    if (subFieldMap != null) {
                        for (List<ApsFormField> subFields : subFieldMap.values()) {
                            if (subFields != null) {
                                collectSubFields(subFields, listOfAllFields);
                            }
                        }
                    }
                }
            }
        }
    }

}

