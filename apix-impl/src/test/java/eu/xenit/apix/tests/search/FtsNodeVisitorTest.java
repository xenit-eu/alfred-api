package eu.xenit.apix.tests.search;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import eu.xenit.apix.alfresco.dictionary.PropertyService;
import eu.xenit.apix.alfresco.search.FtsNodeVisitor;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.search.visitors.SearchSyntaxPrinter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FtsNodeVisitorTest {

    private final static Logger logger = LoggerFactory.getLogger(FtsNodeVisitorTest.class);

    private Map<QName, String> propertyToDataType = new HashMap<QName, String>() {{
        put(new QName("{tenant.model}stringProperty1"), "d:text");
        put(new QName("{tenant.model}stringProperty2"), "d:text");
        put(new QName("{tenant.model}intProperty"), "d:int");
    }};

    @Test
    public void testIntTypeInvalid() {
        SearchSyntaxNode querySyntaxTree = generateAllQuery("5500012345");
        PropertyService propertyService = new PropertyServiceStub(propertyToDataType);

        String ftsQuery = toFts(querySyntaxTree, propertyService);
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant.model}stringProperty1"));
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant.model}stringProperty2"));
        assertThat("Fts search String contains term that shoud have been filtered out", ftsQuery,
                not(containsString("{tenant.model}intProperty")));
    }

    @Test
    public void testIntTypeValid() {
        SearchSyntaxNode querySyntaxTree = generateAllQuery("1500012345");
        PropertyService propertyService = new PropertyServiceStub(propertyToDataType);

        String ftsQuery = toFts(querySyntaxTree, propertyService);
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant.model}stringProperty1"));
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant.model}stringProperty2"));
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant.model}intProperty"));
    }

    private SearchSyntaxNode generateAllQuery(String value) {
        QueryBuilder querySyntaxTree = new QueryBuilder()
                .startAnd()
                .startOr();
        for (QName qName : propertyToDataType.keySet()) {
            querySyntaxTree = querySyntaxTree.property(qName.getValue(), value);
        }
        return querySyntaxTree
                .end()
                .term("type", "{http://www.alfresco.org/model/content/1.0}content")
                .end()
                .create();
    }

    private String toFts(SearchSyntaxNode node, PropertyService propertyService) {
        logger.debug(SearchSyntaxPrinter.Print(node));
        FtsNodeVisitor visitor = new FtsNodeVisitor(propertyService);
        String ret = visitor.visit(node);

        logger.debug(ret);
        return ret;
    }

    private static class PropertyServiceStub extends PropertyService {

        final Map<QName, String> propertyToDataType;

        public PropertyServiceStub(Map<QName, String> propertyToDataType) {
            this.propertyToDataType = propertyToDataType;
        }

        @Override
        public PropertyDefinition GetPropertyDefinition(QName qname) {
            String s = propertyToDataType.get(qname);
            if (s == null) {
                throw new NoSuchElementException("propertyToDataType in Stub did not contain QName: " + qname);
            }
            return definitionWithJustDataType(propertyToDataType.get(qname));
        }

        private PropertyDefinition definitionWithJustDataType(String type) {
            PropertyDefinition def = new PropertyDefinition();
            def.setDataType(new QName(type));
            return def;
        }

    }
}
