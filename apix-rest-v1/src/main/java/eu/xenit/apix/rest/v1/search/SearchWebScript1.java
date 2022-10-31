package eu.xenit.apix.rest.v1.search;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import eu.xenit.apix.rest.v1.ApixV1Webscript;
import eu.xenit.apix.search.ISearchService;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.SearchQueryResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AlfrescoAuthentication(AuthenticationType.USER)
@RestController("eu.xenit.apix.rest.v1.search.SearchWebScriptV1")
public class SearchWebScript1 extends ApixV1Webscript {

    private final ISearchService service;

    public SearchWebScript1(ISearchService service) {
        this.service = service;
    }

    @PostMapping(value = "/v1/search")
    @ApiOperation(value = "Performs a search for nodes", notes ="# Request components\n"
          + "\n"
          + "## query\n"
          + "Object containing subcomponents that build the requested query.\n"
          + "Info about the Search query syntax can be found here: " +
                "https://docs.xenit.eu/alfred-api/stable-user/rest-api\n"
          + "### special search terms:\n"
          + "- type: searches for nodes of that type (for example: \"type\" : \"cm:content\")\n"
          + "- aspect: searches for nodes with that aspect (for example: \"aspect\" : \"cm:titled\")\n"
          + "- noderef: searches the node with that noderef (for example: \"noderef\" :" +
                " \"workspace://SpacesStore/f0d15919-3841-4170-807f-b81d2ebdeb80\")\n"
          + "- parent: searches the nodes with that parent (for example: \"parent\" : " +
                "\"workspace://SpacesStore/f0d15919-3841-4170-807f-b81d2ebdeb80\")\n"
          + "- path: searches the nodes with that path (for example: \"path\" : \"/\")\n"
          + "- category: searches the nodes with that category (for example: \"category\" : " +
                "\"workspace://SpacesStore/f0d15919-3841-4170-807f-b81d2ebdeb80\")\n"
          + "- text: searches the nodes with content containing that text (for example: \"text\" : \"this text\")\n"
          + "- all: searches the nodes with content, cm:name, cm:creator, cm:modifier or cm:author containing " +
                "the value (for example: \"all\" : \"search term\")\n"
          + "- isunset: searches the nodes with where the value of the property is not set (for example: " +
                "\"isunset\" : \"cm:author\")\n"
          + "- isnull: searches the nodes with where the value of the property is null (for example: \"isnull\" :" +
                " \"cm:author\")\n"
          + "- isnotnull: searches the nodes with where the value of the property is not null (for example: " +
                "\"isnotnull\" : \"cm:author\")\n"
          + "- exists: searches the nodes that have the property (for example: \"exists\" : \"cm:author\")\n"
          + "\n"
          + "## paging\n"
          + "`Optional`\n"
          + "\n"
          + "Options to skip over results starting from the top of the result and " +
                "to limit the total number of results.\n"
          + "\n"
          + "## facets\n"
          + "`Optional`\n"
          + "\n"
          + "Options to enable, limit the total amount, minimum number of hits and customize input of facets.\n"
          + "\n"
          + "<b>Note:</b> facets with 0 hits are not returned in the result\n"
          + "\n"
          + "## orderBy\n"
          + "`Optional`\n"
          + "\n"
          + "Options to select the property to order by and the direction of sorting.\n"
          + "\n"
          + "## consistency\n"
          + "`Optional`\n"
          + "\n"
          + "Option to request specific consistency\n"
          + "\n"
          + "## locale\n"
          + "`Optional`\n"
          + "\n"
          + "Options to request specific locale and encoding options\n"
          + "\n"
          + "## workspace\n"
          + "`Optional`\n"
          + "\n"
          + "Options to change the target alfresco workspace\n"
          + "\n"
          + "## highlight\n"
          + "`5.2 and up`\n"
          + "\n"
          + "`Optional`\n"
          + "\n"
          + "Options to change the highlight configuration.\n"
          + "Minimal requirement is the `fields` array, which takes object containing at least the `field` property. " +
            "Each list element specifies a property on which higlighting needs to be applied. " +
            "When no fields are specified, the call defaults to `cm:content` as field.\n"
          + "Full documentation can be found on the alfresco " +
            "[documentation](https://docs.alfresco.com/5.2/concepts/search-api-highlight.html) page.\n"
          + "\n"
          + "# Examples\n"
          + "\n"
          + "Search for the first 10 nodes in the `cm:content` namespace:\n"
          + "```json\n"
          + "{\n"
          + "  \"query\": {\"type\":\"{http://www.alfresco.org/model/content/1.0}content\"},\n"
          + "  \"paging\": {\n"
          + "    \"limit\": 10,\n"
          + "    \"skip\": 0\n"
          + "  },\n"
          + "  \"facets\": {\n"
          + "    \"enabled\": false\n"
          + "  }\n"
          + "}\n"
          + "```\n"
          + "\n"
          + "Search for all nodes with the term 'budget' in the `cm:content` property (fulltext), and show two " +
            "highlighted hits with delimiter \\<highlight>\\</highlight>:\n"
          + "```json\n"
          + "{\n"
          + "    \"query\": {\n"
          + "        \"property\": {\n"
          + "            \"exact\": false,\n"
          + "            \"name\": \"cm:content\",\n"
          + "            \"value\": \"budget\"\n"
          + "        }\n"
          + "    },\n"
          + "    \"highlight\":{\n"
          + "        \"prefix\":\"<highlight>\",\n"
          + "        \"postfix\":\"</highlight>\",\n"
          + "        \"snippetCount\":2,\n"
          + "\t\t\"fields\":[{\"field\":\"cm:content\"}]\n"
          + "    }\n"
          + "}\n"
          + "```")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success", response = SearchQueryResult.class),
            @ApiResponse(code = 400, message = "Failure")})
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "eu.xenit.apix.search.SearchQuery", paramType = "body", name = "body")})
    public ResponseEntity<?> execute(@RequestBody final SearchQuery query) {
        try {
            return writeJsonResponse(service.query(query));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(400).body(illegalArgumentException.getMessage());
        }
    }
}
