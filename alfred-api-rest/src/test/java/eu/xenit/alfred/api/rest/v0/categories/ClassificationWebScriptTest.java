package eu.xenit.alfred.api.rest.v0.categories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.categories.Category;
import eu.xenit.alfred.api.categories.ICategoryService;
import eu.xenit.alfred.api.data.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

class ClassificationWebScriptTest {

    private final ICategoryService categoryService = mock(ICategoryService.class);

    @Test
    void writesTheCategoryTreeAsABareJsonArray() throws IOException {
        when(categoryService.getCategoryTree(new QName("cm:generalclassifiable"))).thenReturn(List.of());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebScriptResponse res = responseWritingTo(out);

        new ClassificationWebScript(categoryService).execute(requestWithAspect("cm:generalclassifiable"), res);

        verify(res).setStatus(200);
        verify(res).setContentType("application/json");
        verify(res).setHeader("Cache-Control", "no-cache");
        // v0 returns the list itself, not v1's {"categories": [...]} wrapper.
        assertEquals("[]", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void acceptsAFullQNameContainingSlashes() throws IOException {
        String fullQName = "{http://www.alfresco.org/model/content/1.0}generalclassifiable";
        when(categoryService.getCategoryTree(new QName(fullQName))).thenReturn(List.<Category>of());

        new ClassificationWebScript(categoryService)
                .execute(requestWithAspect(fullQName), responseWritingTo(new ByteArrayOutputStream()));

        verify(categoryService).getCategoryTree(new QName(fullQName));
    }

    private static WebScriptRequest requestWithAspect(String aspectQName) {
        WebScriptRequest req = mock(WebScriptRequest.class);
        when(req.getServiceMatch()).thenReturn(new Match("/apix/classification/{aspectQName}",
                Map.of("aspectQName", aspectQName), "/apix/classification/"));
        return req;
    }

    private static WebScriptResponse responseWritingTo(ByteArrayOutputStream out) throws IOException {
        WebScriptResponse res = mock(WebScriptResponse.class);
        when(res.getOutputStream()).thenReturn(out);
        return res;
    }
}