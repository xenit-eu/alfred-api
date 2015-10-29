package eu.xenit.apix.rest.v1;

import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Created by Michiel Huygen on 14/03/2016.
 */
@SwaggerDefinition(
        info = @Info(
                description = "This is the swagger specification for Api-X REST API" +
                        "\n\n" +
                        "Examples can be found at: https://xenitsupport.jira.com/wiki/display/APIX/REST+API+Examples",
                version = "2.0.0",
                title = "Api-X REST API",
                //termsOfService = "http://swagger.io/terms/",
                contact = @Contact(name = "XeniT", email = "engineering@xenit.eu", url = "http://www.xenit.eu")
                //license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")
        ),
        //consumes = {"application/json", "application/xml"},
        produces = {"application/json"},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        tags = {@Tag(name = "WIP", description = "Dont use in production!")}
        //tags = {@Tag(name = "metadata", description = "Operations on node metadata")}
)
public class ApixSwaggerDescription {

}
