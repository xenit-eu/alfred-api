package eu.xenit.apix.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Michiel Huygen on 29/03/2016.
 */
@RestController
//@WebScript(baseUri = "/base", families = "My Family", value = "TestDEWebscript")
//@Authentication(AuthenticationType.GUEST)
public class TestDEWebscript1 {

    @GetMapping("/method")
    public void testGet() {

    }

//    @Authentication(AuthenticationType.ADMIN)
    @PostMapping(value = "/method/{param}")
    public void testPost() {

    }
}
