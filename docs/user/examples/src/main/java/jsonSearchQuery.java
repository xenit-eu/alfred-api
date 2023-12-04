import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.json.SearchNodeJsonParser;
import eu.xenit.apix.search.visitors.SearchSyntaxPrinter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

class jsonSearchQuery {

    public static void main(String[] argv) throws IOException, URISyntaxException {
        InputStream inputStream = jsonSearchQuery.class.getResourceAsStream("jsonsearchquery.json");
        ObjectMapper mapper = new SearchNodeJsonParser().getObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        SearchQuery searchQuery = mapper.readValue(inputStream, SearchQuery.class);
        System.out.println(SearchSyntaxPrinter.Print(searchQuery.getQuery()));
    }
}
