package eu.xenit.apix.search.nodes;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
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

}
