package eu.xenit.apix.alfresco42.search;

import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.alfresco42.search.configuration.FacetConfiguration;
import eu.xenit.apix.alfresco42.search.ScriptFacetResult;
import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.SearchQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.xenit.apix.search.nodes.SearchSyntaxNode;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.M2ModelDefinition;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchFacetsServiceImpl42 implements SearchFacetsService {

    private static final Logger logger = LoggerFactory.getLogger(SearchFacetsServiceImpl42.class);

    private FacetConfiguration facetConfiguration;
    private DictionaryService dictionaryService;
    private NodeService nodeService;

    public SearchFacetsServiceImpl42(ServiceRegistry serviceRegistry, FacetConfiguration facetConfiguration) {
        this.facetConfiguration = facetConfiguration;
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    public List<String> filterFacets(SearchQuery.FacetOptions opts, List<String> facets) {
        if (null == opts.getCustom()) {
            return facets;
        }
        List<String> ret = new ArrayList<String>();
        for (String toAdd : facets) {
            if (opts.getCustom().contains(toAdd)) {
                ret.add(toAdd);
            }
        }
        return ret;
    }

    @Override
    @Deprecated
    public void addFacetSearchParameters(SearchQuery.FacetOptions opts, SearchParameters sp, String ftsQuery) {
        this.addFacetSearchParameters(opts, sp, ftsQuery, null);
    }

    @Override
    public void addFacetSearchParameters(SearchQuery.FacetOptions opts, SearchParameters sp, String ftsQuery,
            SearchSyntaxNode searchNode) {
        if (!opts.isEnabled()) {
            return;
        }

        List<String> facetConfig = filterFacets(opts, this.facetConfiguration.getFacetConfig());

        if (logger.isDebugEnabled()) {
            logger.debug("Adding facets to search parameters: " + facetConfig.toString());
        }

        for (String facet : facetConfig) {
            SearchParameters.FieldFacet fieldFacet = new SearchParameters.FieldFacet(facet);

            if (opts.getLimit() != null) {
                fieldFacet.setLimit(opts.getLimit());
            }

            if (opts.getMincount() != null) {
                fieldFacet.setMinCount(opts.getMincount());
            }

            sp.addFieldFacet(fieldFacet);
        }
    }

    @Override
    public List<FacetSearchResult> getFacetResults(SearchQuery.FacetOptions opts, ResultSet rs, SearchParameters sp) {
        if (!opts.isEnabled()) {
            return null;
        }
        Map<String, List<ScriptFacetResult>> facetResults = getFacetResults(sp, rs);

        List<FacetSearchResult> facets = new ArrayList<>(facetResults.size());

        for (Map.Entry<String, List<ScriptFacetResult>> entry : facetResults.entrySet()) {
            facets.add(toFacetSearchResult(entry));
        }
        return facets;
    }

    private FacetSearchResult toFacetSearchResult(Map.Entry<String, List<ScriptFacetResult>> entry) {
        FacetSearchResult ret = new FacetSearchResult();
        ret.setName(entry.getKey());

        List<FacetSearchResult.FacetValue> facetValues = new ArrayList<>(entry.getValue().size());
        Iterator<ScriptFacetResult> iterator = entry.getValue().iterator();
        while (iterator.hasNext()) {
            ScriptFacetResult facetResult = iterator.next();

            if (facetResult.getFacetValue().contains("..")) {
                continue;
            }

            FacetSearchResult.FacetValue facetSearchResult = new FacetSearchResult.FacetValue(
                    facetResult.getFacetValue(), facetResult.getFacetLabel(), facetResult.getHits());
            facetValues.add(facetSearchResult);
        }
        ret.setValues(facetValues);
        return ret;
    }

    private Map<String, List<ScriptFacetResult>> getFacetResults(SearchParameters sp, ResultSet resultSet) {
        Map<String, List<ScriptFacetResult>> result = new LinkedHashMap<String, List<ScriptFacetResult>>();

        List<SearchParameters.FieldFacet> fieldFacets = sp.getFieldFacets();
        if (fieldFacets == null || fieldFacets.size() == 0) {
            return result;
        }

        for (SearchParameters.FieldFacet fieldFacet : fieldFacets) {

            // For each requested facet, get the facet results
            List<Pair<String, Integer>> fieldFacetResults = resultSet.getFieldFacet(fieldFacet.getField());
            if (fieldFacetResults == null || fieldFacetResults.size() == 0) {
                continue;
            }

            String facetField = fieldFacet.getField();

            List<ScriptFacetResult> facetResults = handleFacetResults(fieldFacet, fieldFacetResults);

            // Store facet results per field
            if (facetField.equals("TYPE")) {
                result.put(facetField, facetResults);
            } else {
                result.put(this.getQNameFromFacetField(facetField).toString(), facetResults);
            }
        }
        return result;
    }

    //public for testing
    public List<ScriptFacetResult> handleFacetResults(SearchParameters.FieldFacet facetField,
            List<Pair<String, Integer>> fieldFacetResults) {
        List<ScriptFacetResult> response = new ArrayList<>();
        for (Pair<String, Integer> fieldFacetResult : fieldFacetResults) {
            // Ignore zero hit fields
            int hits = fieldFacetResult.getSecond();
            if (hits <= 0) {
                continue;
            }

            // The stringValue of the facet-value
            String facetValue = fieldFacetResult.getFirst();

            String label = translateLabelValue(facetField.getField(), facetValue);

            response.add(new ScriptFacetResult(facetValue, label, -1, hits));
        }
        return response;
    }

    private String translateLabelValue(String facetField, String facetValue) {
        String response = null;
        if (facetField.equals("TYPE")) {
            // This facet is the TYPE of the node
            QName propQName = getQNameFromFacetField(facetValue);
            TypeDefinition typeDef = dictionaryService.getType(propQName);
            response = typeDef.getTitle(dictionaryService);
            return response;
        }

        QName fieldQname = getQNameFromFacetField(facetField);
        PropertyDefinition facetPropertyDef = dictionaryService.getProperty(fieldQname);

        if (facetPropertyDef == null) {
            return null;
        }

        if (facetPropertyDef.getDataType().getName()
                .equals(QName.createQName("http://www.alfresco.org/model/dictionary/1.0", "category"))) {
            // This facet is a category type field
            NodeRef noderef = new NodeRef(facetValue);
            Serializable nodeName = nodeService.getProperty(noderef, ContentModel.PROP_NAME);
            if (nodeName != null) {
                response = nodeName.toString();
                return response;
            }
        }

        // Final case: if this facet is a property with a listconstraint, use that translation
        for (ConstraintDefinition constraintDefinition : facetPropertyDef.getConstraints()) {
            Constraint constraint = constraintDefinition.getConstraint();
            if (constraint instanceof ListOfValuesConstraint) {
                response = ((ListOfValuesConstraint) constraint).getDisplayLabel(facetValue, dictionaryService);
                return response;
            }
        }
        if (response == null) {
            response = facetValue;
        }
        return response;
    }


    private QName getQNameFromFacetField(String facetField) {
        // Remove suffix
        int index = facetField.indexOf("._");
        String qnameString = facetField;
        if (index >= 0) {
            qnameString = facetField.substring(0, index);
        }

        // Remove prefix
        qnameString = qnameString.replace("@", "");
        qnameString = qnameString.replace("\\", "");

        QName qName = QName.createQName(qnameString);
        return qName;
    }
}
