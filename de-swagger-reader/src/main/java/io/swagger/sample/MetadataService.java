package io.swagger.sample;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.List;

/**
 * Created by Michiel Huygen on 23/11/2015.
 */

@SwaggerDefinition(
        info = @Info(
                description = "This is a sample server",
                version = "1.0.0",
                title = "Swagger Sample Servlet",
                termsOfService = "http://swagger.io/terms/",
                contact = @Contact(name = "Sponge-Bobo", email = "apiteam@swagger.io", url = "http://swagger.io"),
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")
        ),
        consumes = {"application/json", "application/xml"},
        produces = {"application/json", "application/xml"},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        tags = {@Tag(name = "metadata", description = "Operations on node metadata")}
)
@Api(value = "/metadata", produces = "application/json")
public interface MetadataService {

    List<NodeMetadata> getMetadata(List<NodeRef> noderefs);

    //@GET
    //@Path("/")
    @ApiOperation(value = "Retrieve metadata for node")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Pet not found")})
    NodeMetadata getMetadata(
            @ApiParam(required = true, defaultValue = "[noderef to company home]") //@QueryParam("noderef")
                    NodeRef noderef);

    NodeMetadata setMetadata(NodeRef noderef, MetadataChanges metadata);
}
