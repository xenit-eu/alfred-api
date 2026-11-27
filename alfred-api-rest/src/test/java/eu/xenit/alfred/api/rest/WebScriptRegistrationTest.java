package eu.xenit.alfred.api.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.stereotype.Component;

/**
 * Alfresco binds a Java-backed web script to its descriptor by bean name: a descriptor at
 * {@code <path>/<id>.<method>.desc.xml} is served by the bean {@code webscript.<path with dots>.<id>.<method>}.
 * When no such bean exists, Alfresco silently falls back to a template-based DeclarativeWebScript, which then fails at
 * runtime with a missing-template error. This test catches such mismatches at build time.
 */
class WebScriptRegistrationTest {

    private static final String BASE_PACKAGE = "eu.xenit.alfred.api.rest";
    private static final String BEAN_PREFIX = "webscript.";
    private static final String DESCRIPTOR_ROOT = "alfresco/extension/templates/webscripts/";
    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "delete");

    @Test
    void everyWebScriptHasAMatchingDescriptor() throws ClassNotFoundException {
        List<String> problems = new ArrayList<>();
        Set<String> discovered = discoverWebScripts();

        for (String className : discovered) {
            Class<?> webScriptClass = Class.forName(className);
            Component component = webScriptClass.getAnnotation(Component.class);
            if (component == null || !component.value().startsWith(BEAN_PREFIX)) {
                problems.add(className + ": missing @Component(\"webscript.<package>.<script-id>.<method>\")"
                        + (component == null ? "" : ", found @Component(\"" + component.value() + "\")"));
                continue;
            }

            String beanName = component.value();
            String descriptor = descriptorPathFor(beanName);
            if (descriptor == null) {
                problems.add(className + ": bean name '" + beanName + "' does not end in .<script-id>.<method> "
                        + "with method one of " + HTTP_METHODS);
            } else if (webScriptClass.getClassLoader().getResource(descriptor) == null) {
                problems.add(className + ": bean name '" + beanName + "' requires descriptor '" + descriptor
                        + "', which is not on the classpath");
            }
        }

        if (!problems.isEmpty()) {
            fail("Web script registration problems:\n  " + String.join("\n  ", problems));
        }
    }

    @Test
    void scanFindsTheKnownWebScripts() {
        // Guards against the scan silently finding nothing, which would make the test above pass vacuously.
        Set<String> discovered = discoverWebScripts();
        for (String expected : List.of(
                "eu.xenit.alfred.api.rest.v0.metadata.NodeMetadataGetWebScript",
                "eu.xenit.alfred.api.rest.v0.metadata.NodeMetadataPostWebScript",
                "eu.xenit.alfred.api.rest.v0.metadata.NodeMetadataBulkWebScript",
                "eu.xenit.alfred.api.rest.v1.people.PersonByUsernameWebScript",
                "eu.xenit.alfred.api.rest.v1.people.PersonByNodeRefWebScript",
                "eu.xenit.alfred.api.rest.v2.people.PeopleListWebScript",
                "eu.xenit.alfred.api.rest.v2.people.CurrentPersonWebScript",
                "eu.xenit.alfred.api.rest.v2.people.PersonByUsernameWebScript",
                "eu.xenit.alfred.api.rest.v2.people.PersonByNodeRefWebScript",
                "eu.xenit.alfred.api.rest.v2.people.ContainerGroupsWebScript",
                "eu.xenit.alfred.api.rest.v2.groups.GroupsListWebScript",
                "eu.xenit.alfred.api.rest.v2.groups.GroupPeopleGetWebScript",
                "eu.xenit.alfred.api.rest.v2.groups.GroupPeoplePutWebScript",
                "eu.xenit.alfred.api.rest.v2.groups.SubgroupsGetWebScript",
                "eu.xenit.alfred.api.rest.v2.groups.SubgroupsPutWebScript",
                "eu.xenit.alfred.api.rest.v1.dictionary.DictionaryNamespacesWebScript",
                "eu.xenit.alfred.api.rest.v1.dictionary.DictionaryPropertiesWebScript",
                "eu.xenit.alfred.api.rest.v1.dictionary.DictionaryPropertyWebScript",
                "eu.xenit.alfred.api.rest.v1.dictionary.DictionaryTypesWebScript",
                "eu.xenit.alfred.api.rest.v1.dictionary.DictionaryTypeWebScript",
                "eu.xenit.alfred.api.rest.v1.dictionary.DictionaryAspectsWebScript",
                "eu.xenit.alfred.api.rest.v1.dictionary.DictionaryAspectWebScript",
                "eu.xenit.alfred.api.rest.v1.logs.LogWebScript",
                "eu.xenit.alfred.api.rest.v1.version.VersionWebScript",
                "eu.xenit.alfred.api.rest.v1.categories.CategoriesWebScript",
                "eu.xenit.alfred.api.rest.v1.bulk.BulkWebScript",
                "eu.xenit.alfred.api.rest.v1.nodes.UploadWebscript",
                "eu.xenit.alfred.api.rest.v1.nodes.SetNodeContentWebscript")) {
            assertTrue(discovered.contains(expected), "Expected to discover " + expected + " in " + discovered);
        }
    }

    /**
     * Same discovery rule as the component scan in alfresco/module/alfred-api/module-context.xml: concrete classes
     * assignable to AbstractWebScript, regardless of annotations.
     */
    private static Set<String> discoverWebScripts() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(AbstractWebScript.class));
        Set<String> classNames = new TreeSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            classNames.add(candidate.getBeanClassName());
        }
        return classNames;
    }

    /**
     * {@code webscript.a.b.c.<id>.<method>} maps to {@code <root>/a/b/c/<id>.<method>.desc.xml}. Script ids therefore
     * must not contain dots, otherwise the mapping is ambiguous.
     */
    static String descriptorPathFor(String beanName) {
        String[] parts = beanName.substring(BEAN_PREFIX.length()).split("\\.");
        if (parts.length < 3 || !HTTP_METHODS.contains(parts[parts.length - 1])) {
            return null;
        }
        String method = parts[parts.length - 1];
        String scriptId = parts[parts.length - 2];
        String packagePath = String.join("/", List.of(parts).subList(0, parts.length - 2));
        return DESCRIPTOR_ROOT + packagePath + "/" + scriptId + "." + method + ".desc.xml";
    }
}
