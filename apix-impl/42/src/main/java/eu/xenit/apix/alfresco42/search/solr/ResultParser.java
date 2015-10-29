package eu.xenit.apix.alfresco42.search.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.SearchQueryResult;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Stan on 22-Feb-16.
 */
public class ResultParser {

    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private ApixToAlfrescoConversion convertor;

    public ResultParser(NodeService nodeService, NamespaceService namespaceService, DictionaryService dictionaryService,
            ApixToAlfrescoConversion apixToAlfrescoConversion) {
        this.nodeService = nodeService;
        this.namespaceService = namespaceService;
        this.dictionaryService = dictionaryService;

        this.convertor = apixToAlfrescoConversion;
    }

    public SearchQueryResult parseResult(String input, Integer facetLimit) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(input);

            int numFound = tree.get("response").get("numFound").asInt();

            List<String> nodeRefs = new ArrayList<>();

            JsonNode nodes = tree.get("response").get("docs");
            for (int i = 0; i < nodes.size(); i++) {
                JsonNode node = nodes.get(i);

                NodeRef ref = this.nodeService.getNodeRef(node.get("DBID").get(0).asLong());

                nodeRefs.add(ref.toString());
            }

            JsonNode facets = tree.get("facet_counts").get("facet_fields");

            List<FacetSearchResult> facetSearchResults = new ArrayList<>();

            Iterator<String> iterator = facets.fieldNames();
            while (iterator.hasNext()) {
                String key = iterator.next();
                QName keyQName = this.facetTokenToQname(key);

                FacetSearchResult facetSearchResult = new FacetSearchResult();
                facetSearchResult.setName(universalizeFacetToken(key));

                List<FacetSearchResult.FacetValue> facetValues = new ArrayList<>();
                facetSearchResult.setValues(facetValues);
//                if (facetLimit != null) {
//                    facetSearchResult.setLimit(facetLimit);
//                }

                Boolean needsTranslation = this.isListContstraint(keyQName);
                JsonNode jsonFacet = facets.get(key);
                for (int i = 0; i < jsonFacet.size(); i = i + 2) {
                    String facetKey = jsonFacet.get(i).textValue();
                    int amount = jsonFacet.get(i + 1).asInt();

                    FacetSearchResult.FacetValue facetValue = null;
                    if (needsTranslation) {
                        facetValue = new FacetSearchResult.FacetValue(facetKey, this.getFacetLabel(keyQName, facetKey),
                                amount);
                    } else {
                        facetValue = new FacetSearchResult.FacetValue(facetKey, facetKey, amount);
                    }

                    facetValues.add(facetValue);
                }

                facetSearchResults.add(facetSearchResult);
            }

            SearchQueryResult searchQueryResult = new SearchQueryResult();
            searchQueryResult.setTotalResultCount(numFound);
            searchQueryResult.setNoderefs(nodeRefs);
            searchQueryResult.setFacets(facetSearchResults);

            return searchQueryResult;
        } catch (IOException ioe) {
            // crap!
            return null;
        }

    }

    private Boolean isListContstraint(QName facet) {

        PropertyDefinition propDef = this.dictionaryService.getProperty(facet);

        if (propDef == null) { // cant have a list constraint if it isn't a property he!
            return false;
        }

        List<ConstraintDefinition> constraints = propDef.getConstraints();
        if (constraints.size() == 0) { // if empty = no constraints
            return false;
        }

        for (ConstraintDefinition constraintDef : constraints) {
            Constraint constraint = constraintDef.getConstraint();

            if (constraint.getType().equals(ListOfValuesConstraint.CONSTRAINT_TYPE)) { // booyah
                return true;
            }
        }

        return false;
    }

    private String getFacetLabel(QName propQName, String facetValue) {
        PropertyDefinition propertyDefinition = this.dictionaryService.getProperty(propQName);

        if (propertyDefinition == null) {
            return null;
        }

        ListOfValuesConstraint listOfValuesConstraint = null;
        for (ConstraintDefinition constraintDefinition : propertyDefinition.getConstraints()) {
            if (constraintDefinition.getConstraint().getType().equals(ListOfValuesConstraint.CONSTRAINT_TYPE)) {
                listOfValuesConstraint = (ListOfValuesConstraint) constraintDefinition.getConstraint();
                break;
            }
        }

        if (listOfValuesConstraint == null) {
            return null;
        }

        return listOfValuesConstraint.getDisplayLabel(facetValue, new StaticMessageLookup());
    }

    private QName facetTokenToQname(String facetToken) {
        String token = universalizeFacetToken(facetToken);
        return convertor.alfresco(new eu.xenit.apix.data.QName(token));
    }

    private String universalizeFacetToken(String facetToken) {
        String token = facetToken;

        if (token.startsWith("@")) {
            token = token.substring(token.indexOf("@") + 1);
        }

        if (token.endsWith(".__.u")) {
            token = token.substring(0, token.lastIndexOf(".__.u"));
        }

        if (token.endsWith(".__")) {
            token = token.substring(0, token.lastIndexOf(".__"));
        }

        if (token.endsWith(".u")) {
            token = token.substring(0, token.lastIndexOf(".u"));
        }

        return token;
    }

}
