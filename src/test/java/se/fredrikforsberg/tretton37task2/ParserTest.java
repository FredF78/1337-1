package se.fredrikforsberg.tretton37task2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.MalformedURLException;
import java.util.List;

//@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Parser.class, Cli2.class})

public class ParserTest {

    @Autowired
    private Parser parser;

    @Test
    @Disabled
    public void testParsePageToPaths() throws MalformedURLException {
        String actualPage = "http://books.toscrape.com/catalogue/a-light-in-the-attic_1000/index.html";
        String expectedPath = "/catalogue/a-light-in-the-attic_1000/index.html";
        String path = parser.getPathFromURL(actualPage);
        Assertions.assertEquals(expectedPath, path);
    }

    @Test
    @Disabled
    public void testParsePartsFromPath() {
        String actualPath = "/catalogue/a-light-in-the-attic_1000/index.html";
        List<String> parts = parser.getPartsFromPath(actualPath);
        Assertions.assertEquals(3, parts.size());
    }

    @Test
    @Disabled
    public void testHasNoPath() {
        String url = "http://example.com";
        boolean actualResult = parser.hasPath(url);
        Assertions.assertFalse(actualResult);
    }
    @Test
    @Disabled
    public void testHasNoPath2() {
        String url = "http://books.toscrape.com/";
        boolean actualResult = parser.hasPath(url);
        Assertions.assertFalse(actualResult);
    }
    @Test
    @Disabled
    public void testHasPathPath() {
        String url = "http://books.toscrape.com/catalogue/categories";
        boolean actualResult = parser.hasPath(url);
        Assertions.assertTrue(actualResult);
    }
    @Test
    @Disabled
    public void testHasPathPath2() {
        String url = "http://books.toscrape.com/catalogue";
        boolean actualResult = parser.hasPath(url);
        Assertions.assertTrue(actualResult);
    }

    @Test
    @Disabled
    public void testGetPathNoHost() {
        String url = "http://books.toscrape.com/catalogue/index.html";
        String actualResult = parser.getPathNoHost(url);
        String expected = "/catalogue/index.html";
        Assertions.assertEquals(expected, actualResult);
    }

    @Test
    @Disabled
    public void testGetPathNoFilenameFromURL() {
        String url = "http://books.toscrape.com/catalogue/categories/index.html";
        String expected = "/catalogue/categories";
        String actual = parser.getPathNoFilenameFromURL(url);
    }

}
