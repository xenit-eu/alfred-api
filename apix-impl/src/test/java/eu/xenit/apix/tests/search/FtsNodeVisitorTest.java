package eu.xenit.apix.tests.search;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.xenit.apix.alfresco.dictionary.PropertyService;
import eu.xenit.apix.alfresco.search.FtsNodeVisitor;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.search.QueryBuilder;
import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import eu.xenit.apix.search.visitors.SearchSyntaxPrinter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FtsNodeVisitorTest {

    private final static Logger logger = LoggerFactory.getLogger(FtsNodeVisitorTest.class);

    private Map<String, String> propertyToDataType = new HashMap<String, String>() {{
        put("{tenant}stringProperty1", "d:text");
        put("{tenant}stringProperty2", "d:text");
        put("{tenant}intProperty", "d:int");
    }};

    @Test
    public void testIntTypeInvalid() {
        SearchSyntaxNode querySyntaxTree = generateAllQuery("5500012345");
        PropertyService propertyService = generatePropertyServiceMock();

        String ftsQuery = toFts(querySyntaxTree, propertyService);
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant}stringProperty1"));
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant}stringProperty2"));
        assertThat("Fts search String contains term that shoud have been filtered out", ftsQuery,
                not(containsString("{tenant}intProperty")));
    }

    @Test
    public void testIntTypeValid() {
        SearchSyntaxNode querySyntaxTree = generateAllQuery("1500012345");
        PropertyService propertyService = generatePropertyServiceMock();

        String ftsQuery = toFts(querySyntaxTree, propertyService);
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant}stringProperty1"));
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant}stringProperty2"));
        assertThat("Fts search String does not contain wanted term", ftsQuery,
                containsString("{tenant}intProperty"));
    }

    private SearchSyntaxNode generateAllQuery(String value) {
        QueryBuilder querySyntaxTree = new QueryBuilder()
                .startAnd()
                .startOr();
        for (String property : propertyToDataType.keySet()) {
            querySyntaxTree = querySyntaxTree.property(property, value);
        }
        return querySyntaxTree
                .end()
                .term("type", "{http://www.alfresco.org/model/content/1.0}content")
                .end()
                .create();
    }

    private PropertyService generatePropertyServiceMock() {
        PropertyService propertyService = mock(PropertyService.class);
        for (Entry<String, String> entry : propertyToDataType.entrySet()) {
            PropertyDefinition def = new PropertyDefinition();
            def.setDataType(new QName(entry.getValue()));
            when(propertyService.GetPropertyDefinition(new QName(entry.getKey())))
                    .thenReturn(def);
        }
        return propertyService;
    }


    private String toFts(SearchSyntaxNode node, PropertyService propertyService) {
        logger.debug(SearchSyntaxPrinter.Print(node));
        FtsNodeVisitor visitor = new FtsNodeVisitor(propertyService);
        String ret = visitor.visit(node);

        logger.debug(ret);
        return ret;
    }
}
