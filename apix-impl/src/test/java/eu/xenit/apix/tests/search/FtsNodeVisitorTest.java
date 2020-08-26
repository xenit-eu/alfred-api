package eu.xenit.apix.tests.search;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import eu.xenit.apix.alfresco.dictionary.PropertyService;
import eu.xenit.apix.alfresco.search.FtsNodeVisitor;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.search.nodes.TermSearchNode;
import eu.xenit.apix.search.visitors.SearchSyntaxPrinter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FtsNodeVisitorTest {

    private final static Logger logger = LoggerFactory.getLogger(FtsNodeVisitorTest.class);
    private final static String IS_UNSET = "isunset";
    private final static String IS_NULL = "isnull";
    private final static String IS_NOT_NULL = "isnotnull";
    private final static String EXISTS = "exists";

    private Map<QName, String> propertyToDataType = new HashMap<QName, String>() {{
        put(new QName("{tenant.model}stringProperty1"), "{http://www.alfresco.org/model/dictionary/1.0}text");
        put(new QName("{tenant.model}stringProperty2"), "{http://www.alfresco.org/model/dictionary/1.0}text");
        put(new QName("{tenant.model}intProperty"), "{http://www.alfresco.org/model/dictionary/1.0}int");
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

    @Test
    public void testIsUnsetTerm() {
        String ftsQuery = convertToFtsTerm(IS_UNSET);
        String expectedResult = "ISUNSET:\"prefix:test_prop\"";
        assertEquals(expectedResult, ftsQuery);
    }

    @Test
    public void testIsNullTerm() {
        String ftsQuery = convertToFtsTerm(IS_NULL);
        String expectedResult = "ISNULL:\"prefix:test_prop\"";
        assertEquals(expectedResult, ftsQuery);
    }

    @Test
    public void testIsNotNullTerm() {
        String ftsQuery = convertToFtsTerm(IS_NOT_NULL);
        String expectedResult = "ISNOTNULL:\"prefix:test_prop\"";
        assertEquals(expectedResult, ftsQuery);
    }

    @Test
    public void testExistsTerm() {
        String ftsQuery = convertToFtsTerm(EXISTS);
        String expectedResult = "EXISTS:\"prefix:test_prop\"";
        assertEquals(expectedResult, ftsQuery);
    }

    private String convertToFtsTerm(String apixTerm) {
        PropertyService propertyService = new PropertyServiceStub(propertyToDataType);
        FtsNodeVisitor ftsnodeVisitor = new FtsNodeVisitor(propertyService);
        TermSearchNode searchNode = new TermSearchNode(apixTerm, "prefix:test_prop");

        return ftsnodeVisitor.visit(searchNode);
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
