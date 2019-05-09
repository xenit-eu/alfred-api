package eu.xenit.apix.alfresco60;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
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
            SearchFacetsService facetService, ApixToAlfrescoConversion apixToAlfrescoConversion) {
        super(searchService, facetService, apixToAlfrescoConversion);
    }

    @Override
    protected SearchParameters buildSearchParameters(SearchQuery postQuery) {
        // Build parameters normally
        SearchParameters parameters = super.buildSearchParameters(postQuery);

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
