package eu.xenit.apix.alfresco60;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.dictionary.PropertyService;
import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.alfresco.search.SearchService;
import eu.xenit.apix.search.Highlights;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQuery.HighlightOptions;
import eu.xenit.apix.search.SearchQueryResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("eu.xenit.apix.search.SearchService")
@OsgiService
public class SearchServiceImpl60 extends SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl60.class);

    @Autowired
    public SearchServiceImpl60(org.alfresco.service.cmr.search.SearchService searchService,
            SearchFacetsService facetService, ApixToAlfrescoConversion apixToAlfrescoConversion, PropertyService propertyService) {
        super(searchService, facetService, apixToAlfrescoConversion, propertyService);
    }

    @Override
    protected SearchParameters buildSearchParameters(SearchQuery postQuery) {
        // Build parameters normally
        SearchParameters parameters = super.buildSearchParameters(postQuery);

        /*
         * Dont set highlights if there is no searchTerm and the query length surpasses the cap.
         * The cap on the querylength is set at 600 characters to have a generous margin on the query, but also maintain
         * a reasonable margin for other query parameters.
         *
         * We do this because the alfresco/solr implementation of highlights, which takes the searchTerms or search query
         * and adds it in the querystring of the request between alfresco and solr.
         * This breaks when:
         * There are no searchTerms in the query; Alfresco does not set these, and apix does not yet support these.
         * AND
         * The query is too long; if there are no searchTerms given, alfresco will instead use the full search query to
         * add to the querystring. For long queries this might violate the url parameter length restriction (around 2000
         *  chars. See appropriate RFC for exact number.) and trigger a 400 response in the solr.
         *
         * This might be fixable by learning how the searchTerms are supposed to work (but these are largely undocumented.
         * Best lead: solr docs on hl.q) and supplying these.
         * Or we could suggest to alfresco (and solr?) development to pass these params in the request body.
         * */
        String searchTerm = parameters.getSearchTerm();
        if ((searchTerm == null || searchTerm.isEmpty()) && parameters.getQuery().length() > 600) {
            return parameters;
        }
        // Extend the parameters with highlights, if they were defined + convert them to Alfresco's internal format
        if (postQuery.getHighlight() != null) {
            HighlightOptions options = postQuery.getHighlight();
            GeneralHighlightParameters highlightParameters = new GeneralHighlightParameters(
                    options.getSnippetCount(),
                    options.getFragmentSize(),
                    options.getMergeContiguous(),
                    options.getPrefix(),
                    options.getPostfix(),
                    options.getMaxAnalyzedCharacters(),
                    options.getUsePhraseHighlighter(),
                    options.getFields().stream()
                            .map(f -> new FieldHighlightParameters(f.field, f.snippetCount, f.fragmentSize,
                                    f.mergeContinuous, f.prefix, f.suffix)).collect(Collectors.toList()));

            parameters.setHighlight(highlightParameters);
        }
        return parameters;
    }

    @Override
    protected SearchQueryResult processResults(ResultSet rs, SearchQuery postQuery, SearchParameters searchParameters) {
        // Process results normally
        SearchQueryResult results = super.processResults(rs, postQuery, searchParameters);

        // Also store term hit highlights in output-model (if present)
        Map<String, List<Highlights.HighlightResult>> highlightResults = rs.getHighlighting().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> e.getValue().stream()
                                .map(p -> new Highlights.HighlightResult(p.getFirst(), p.getSecond()))
                                .collect(Collectors.toList())
                ));
        results.setHighlights(new Highlights(highlightResults));
        return results;
    }
}
