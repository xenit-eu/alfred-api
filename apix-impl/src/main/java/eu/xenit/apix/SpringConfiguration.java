package eu.xenit.apix;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {
        Version.class,
})
public class SpringConfiguration {

}
