package eu.xenit.apix.search.nodes;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class PropertySearchNodeTest {

    @Test
    public void setName_regularOperation() {
        PropertySearchNode searchNode = new PropertySearchNode();
        searchNode.setName("cm:name");
        assertEquals("cm:name", searchNode.getName());
    }

    @Test
    public void setName_EscapesSpecialCharacters() {
        PropertySearchNode searchNode = new PropertySearchNode();
        String specialCharacters = "!@%^&*()-=+[];?,<>|";
        for (int x = 0; x < specialCharacters.length(); x++) {
            //no iteration over string so here we are
            char specialChar = specialCharacters.charAt(x);
            searchNode.setName("sys:store" + specialChar + "identifier");
            assertEquals("sys:store\\" + specialChar + "identifier", searchNode.getName());
        }
    }

    @Test
    public void escapeName_dbid() {
        String unescaped = "sys:node-dbid";
        String escaped = PropertySearchNode.escapeName(unescaped);
        assertEquals("dbid not properly escaped", "sys:node\\-dbid", escaped);
    }

    @Test
    public void unescapeName_dbid() {
        String escaped = "sys:node\\-dbid";
        String unescaped = PropertySearchNode.unescapeName(escaped);
        assertEquals("dbid not properly unescaped", "sys:node-dbid", unescaped);
    }

    @Test
    public void escape_roundtripEquals() {
        List<String> strings = Arrays.asList("sys:node-dbid", "!@%^&*()-=+[];?,<>|", "nothing:special");
        List<String> collect = strings.stream()
                .map(PropertySearchNode::escapeName)
                .map(PropertySearchNode::unescapeName)
                .collect(Collectors.toList());
        assertEquals("Round-trip of escaping and unescaping did not yield initial values", strings, collect);

    }

}
