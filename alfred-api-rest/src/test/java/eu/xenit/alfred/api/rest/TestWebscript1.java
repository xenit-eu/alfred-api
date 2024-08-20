package eu.xenit.alfred.api.rest;

import com.gradecak.alfresco.mvc.annotation.AlfrescoAuthentication;
import com.gradecak.alfresco.mvc.annotation.AuthenticationType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AlfrescoAuthentication(AuthenticationType.GUEST)
public class TestWebscript1 {

    @GetMapping("/method")
    public void testGet() {

    }

    @AlfrescoAuthentication(AuthenticationType.ADMIN)
    @PostMapping(value = "/method/{param}")
    public void testPost() {

    }
}
