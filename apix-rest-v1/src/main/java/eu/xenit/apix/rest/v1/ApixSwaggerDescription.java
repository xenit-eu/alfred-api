package eu.xenit.apix.rest.v1;

import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

/**
 * Created by Michiel Huygen on 14/03/2016.
 */
@SwaggerDefinition(
        info = @Info(
                description = "This is the swagger specification for Api-X REST API" +
                        "\n\n" +
                        "Examples can be found at: https://docs.xenit.eu/alfred-api",
                version = "2.0.0",
                title = "Api-X REST API",
                //termsOfService = "http://swagger.io/terms/",
                contact = @Contact(name = "XeniT", email = "engineering@xenit.eu", url = "http://www.xenit.eu"),
                license = @License(name = "GNU Lesser General Public License v3", url = "https://www.gnu.org/licenses/lgpl-3.0.txt")
        ),
        //consumes = {"application/json", "application/xml"},
        produces = {"application/json"},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        tags = {@Tag(name = "WIP", description = "Dont use in production!")}
        //tags = {@Tag(name = "metadata", description = "Operations on node metadata")}
)
public class ApixSwaggerDescription {

}
