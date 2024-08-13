package eu.xenit.alfred.api.alfresco.search;

import eu.xenit.alfred.api.search.FacetSearchResult;
import eu.xenit.alfred.api.search.SearchQuery;
import eu.xenit.alfred.api.search.nodes.SearchSyntaxNode;
import eu.xenit.alfred.api.translation.ITranslationService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.jscript.ScriptFacetResult;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetHelper;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabel;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandler;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandlerRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Note: To *temporarily* get working syntax highlighting and IDE features in this file: change the root
// build.gradle to have alfresco_4_version = "5.0.d" rather than "4.2.f". Change it back before committing.
@Component
public class SearchFacetsServiceImpl implements SearchFacetsService {

    private static final Logger logger = LoggerFactory.getLogger(SearchFacetsServiceImpl.class);
    private FacetLabelDisplayHandlerRegistry facetLabelDisplayHandlerRegistry;
    private SolrFacetHelper solrFacetHelper;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private SolrFacetService facetService;
    private ITranslationService translationService;

    // This file might give inspection error due to being 5.x specific.
    // Intellij can't handle this file being reused in different libs.
    @Autowired
    public SearchFacetsServiceImpl(ServiceRegistry serviceRegistry, SolrFacetService solrFacetsService,
            ITranslationService translationService) {
        facetService = solrFacetsService;
        this.translationService = translationService;
        facetLabelDisplayHandlerRegistry = serviceRegistry.getFacetLabelDisplayHandlerRegistry();
        solrFacetHelper = serviceRegistry.getSolrFacetHelper();
        dictionaryService = serviceRegistry.getDictionaryService();
        nodeService = serviceRegistry.getNodeService();
    }

    public List<SolrFacetProperties> filterFacets(SearchQuery.FacetOptions opts, List<SolrFacetProperties> facets) {
        List<SolrFacetProperties> enabledFacets = new ArrayList<>();
        for (SolrFacetProperties facet : facets) {
            if (facet.isEnabled()) {
                enabledFacets.add(facet);
            }
        }

        if (null == opts.getCustom()) {
            return enabledFacets;
        }

        List<SolrFacetProperties> ret = new ArrayList<SolrFacetProperties>();
        for (SolrFacetProperties toAdd : enabledFacets) {
            QName facetQName = toAdd.getFacetQName();
            String facetQNameString = facetQName.getNamespaceURI().isEmpty()
                    ? facetQName.getLocalName() : facetQName.toString();
            if (opts.getCustom().contains(facetQNameString)) {
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

        List<SolrFacetProperties> facets = filterFacets(opts, facetService.getFacets());

        for (SolrFacetProperties field : facets) {
            QName facetQName = field.getFacetQName();
            if (facetQName == null) {
                logger.error("Facet with id ({}) has a facetQName of null. "
                        + "This configured facet does not correctly link to a property in the document model."
                        + "\n Facet field config: {}", field.getFilterID(), field.toString());
                continue;
            }

            final String fieldId = "@" + facetQName.toString();

            // Check if this facet is configured to use bucketing
            // Examplex: date-time ranges, content-size, ...
            if (solrFacetHelper.hasFacetQueries(fieldId)) {
                // There are buckets configured for this field
                // We lookup the preconfigured backups
                List<String> facetQueries = solrFacetHelper.getFacetQueries(fieldId);
                //this.addBucketedFacetQuery(sp, field.getFacetQName().toString(), facetQueries.toArray(new String[facetQueries.size()]), req.getQueryString());

                // Workaround for ACE-1605
                String query = ftsQuery;//req.getQueryString();
                QName fieldFacetQName = facetQName;
                if (query.indexOf(fieldFacetQName.toString()) < 0) {
                    // If the query does not contain this field, ask for all buckets
                    for (String fq : facetQueries) {
                        sp.addFacetQuery(fq);
                    }
                } else {
                    // The queryString contains this field, it's no use asking for all the buckets
                    // because they will return with 0 hits whatsoever.  Try to parse and convertQuery that
                    // into a usable bucket.

                    // Previously we used solrFacetHelper.createFacetQueriesFromSearchQuery - but that turned out to be
                    // very fragile and buggy - see https://xenitsupport.jira.com/browse/ALFREDAPI-347
                    // For backwards compatability, this is still in use when using the @Deprecated overload
                    // that calls into this method with argument SearchSyntaxNode = null
                    if (searchNode != null) {
                        List<String> filterQueries = searchNode
                                .accept(new FtsFilterQueryNodeVisitor(fieldFacetQName.toString()));
                        for (String fq : filterQueries) {
                            sp.addFacetQuery(fq);
                        }
                    } else {
                        // @Deprecated AND buggy
                        String fq = solrFacetHelper
                                .createFacetQueriesFromSearchQuery(fieldFacetQName.toString(), query);
                        if (fq != null) {
                            sp.addFacetQuery(fq);
                        }
                    }
                }
            } else {
                // The facet does not have buckets (it's not a range like a date)
                final SearchParameters.FieldFacet fieldFacet;

                // Fields that need special handling, default:
                // SITE, TAG, ANCESTOR, PARENT, ASPECT, TYPE, OWNER
                // Source: solr-facets-context.xml
                if (facetQName.getNamespaceURI().isEmpty() && solrFacetHelper
                        .isSpecialFacetId(facetQName.getLocalName())) {
                    fieldFacet = new SearchParameters.FieldFacet(facetQName.getLocalName());
                } else {
                    fieldFacet = new SearchParameters.FieldFacet(fieldId);
                }
//TODO: set limit
//                if (facetLimit != null)
//                    fieldFacet.setLimit(facetLimit);
//                if (facetMinCount != null)
//                    fieldFacet.setMinCount(facetMinCount);

                sp.addFieldFacet(fieldFacet);
            }
        }

    }

    @Override
    public List<FacetSearchResult> getFacetResults(SearchQuery.FacetOptions opts, ResultSet rs, SearchParameters sp) {
        if (!opts.isEnabled()) {
            return null;
        }
        Map<String, List<ScriptFacetResult>> facetResults = getFacetResults(sp, rs);

        ArrayList<FacetSearchResult> facets = new ArrayList<>();

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
            String fieldQname = facetTokenToQname(fieldFacet.getField());
            List<ScriptFacetResult> facetResults = handleFacetResults(fieldFacet, fieldFacetResults);

            // Store facet results per field
            result.put(fieldQname, facetResults);
        }

        Set<Map.Entry<String, Integer>> facetQueries = resultSet.getFacetQueries().entrySet();
        for (Map.Entry<String, Integer> entry : facetQueries) {
            // Ignore zero hit facet queries
            if (entry.getValue() <= 0) {
                continue;
            }

            String key = entry.getKey();
            // for example the key could be: {!afts}@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-1DAY TO NOW/DAY+1DAY]
            // facetTokenName => @{http://www.alfresco.org/model/content/1.0}created
            // qName => {http://www.alfresco.org/model/content/1.0}created
            // 7 => {!afts}
            key = key.replace("{!afts}", "");
            String facetTokenName = key.substring(0, key.indexOf(":["));
            String qName = facetTokenToQname(facetTokenName);

            // Retrieve the previous facet queries
            List<ScriptFacetResult> fqs = result.get(qName);
            if (fqs == null) {
                fqs = new ArrayList<>();
            }

            // Get the handler for this qName
            FacetLabelDisplayHandler handler = facetLabelDisplayHandlerRegistry.getDisplayHandler(facetTokenName);
            String val = key.substring(key.indexOf(":[") + 1);
            FacetLabel facetLabel = (handler == null) ? new FacetLabel(val, val, -1) : handler.getDisplayLabel(key);
            //facetHandler failed to find a valid facetLabel and returns key as label so skipping facet (bug with date range search)
            if (facetLabel.getLabel().equals(key)) {
                continue;
            }
            // See if we have a nice textual version of this label
            String label = this.translationService.getMessageTranslation(facetLabel.getLabel());
            fqs.add(
                    new ScriptFacetResult(facetLabel.getValue(),
                            label,
                            facetLabel.getLabelIndex(),
                            entry.getValue())
            );
            result.put(qName, fqs);
        }
        return result;
    }

    private List<ScriptFacetResult> handleFacetResults(FieldFacet fieldFacet,
            List<Pair<String, Integer>> fieldFacetResults) {
        String fieldQname = facetTokenToQname(fieldFacet.getField());
        List<ScriptFacetResult> response = new ArrayList<>();
        // Find custom display handler for a certain facet field
        FacetLabelDisplayHandler handler = facetLabelDisplayHandlerRegistry
                .getDisplayHandler(fieldFacet.getField());

        if (handler == null) {
            // If property is a list of values constraint, use those translations
            PropertyDefinition propertyDefinition = dictionaryService.getProperty(QName.createQName(fieldQname));
            if (propertyDefinition == null) {
                logger.error("Property definition of " + fieldQname + " is null.");
            } else {
                handler = CreateFacetLabelDisplayHandler(propertyDefinition);
            }
        }
        for (Pair<String, Integer> fieldFacetResult : fieldFacetResults) {
            // Ignore zero hit fields
            int hits = fieldFacetResult.getSecond();
            if (hits <= 0) {
                continue;
            }

            // The stringValue of the facet-value
            String facetValue = fieldFacetResult.getFirst();

            String label = (handler == null) ? facetValue : handler.getDisplayLabel(facetValue).getLabel();

            response.add(new ScriptFacetResult(facetValue, label, -1, hits));
        }
        return response;
    }

    private FacetLabelDisplayHandler CreateFacetLabelDisplayHandler(PropertyDefinition propertyDefinition) {
        DataTypeDefinition dataType = propertyDefinition.getDataType();
        if (DataTypeDefinition.CATEGORY.equals(dataType.getName())) {
            return new CategoryFacetLabelDisplayHandler(nodeService);
        } else {
            return ListOfValuesFacetLabelDisplayHandler.createFromProperty(propertyDefinition, dictionaryService);
        }
    }


    // @{http://www.alfresco.org/model/content/1.0}created comes in,
    // {http://www.alfresco.org/model/content/1.0}created goes out :-)
    private String facetTokenToQname(String facetToken) {
        return facetToken.charAt(0) == '@' ? facetToken.substring(1) : facetToken;
    }

    /**
     * Implementation of a facet label display handler that is used for properties with a list-of-values constraint
     */
    public static class ListOfValuesFacetLabelDisplayHandler implements FacetLabelDisplayHandler {

        private final MessageLookup messageLookup;
        private final ListOfValuesConstraint constraint;

        private ListOfValuesFacetLabelDisplayHandler(ListOfValuesConstraint constraint, MessageLookup messageLookup) {
            this.constraint = constraint;
            this.messageLookup = messageLookup;
        }

        @Override
        public FacetLabel getDisplayLabel(String value) {
            return new FacetLabel(value, constraint.getDisplayLabel(value, messageLookup), -1);
        }

        public static FacetLabelDisplayHandler createFromProperty(PropertyDefinition propertyDefinition,
                MessageLookup messageLookup) {
            for (ConstraintDefinition constraintDefinition : propertyDefinition.getConstraints()) {
                Constraint constraint = constraintDefinition.getConstraint();
                if (constraint instanceof ListOfValuesConstraint) {
                    return new ListOfValuesFacetLabelDisplayHandler((ListOfValuesConstraint) constraint, messageLookup);
                }
            }
            return null;
        }

    }

    public static class CategoryFacetLabelDisplayHandler implements FacetLabelDisplayHandler {

        private NodeService nodeService;

        private CategoryFacetLabelDisplayHandler(NodeService nodeService) {
            this.nodeService = nodeService;
        }

        @Override
        public FacetLabel getDisplayLabel(String value) {
            NodeRef nodeRef = new NodeRef(value);
            try {
                Serializable nameProperty = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                if (nameProperty == null) {
                    return new FacetLabel(value, value, -1);
                }

                String name = DefaultTypeConverter.INSTANCE.convert(String.class, nameProperty);
                return new FacetLabel(value, name, -1);
            } catch (InvalidNodeRefException ex) {
                logger.error("Node with node reference {} could not be found", nodeRef);
                return new FacetLabel(value, value, -1);
            }
        }
    }
}
