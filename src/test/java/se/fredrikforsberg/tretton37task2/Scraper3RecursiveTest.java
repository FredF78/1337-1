package se.fredrikforsberg.tretton37task2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@Profile("test")
@ExtendWith(MockitoExtension.class)
public class Scraper3RecursiveTest {

    @Autowired
    private Scraper3Recursive scraper;

    @BeforeEach
    public void setupMock() {
        String path = "";
        try (MockedStatic mocked = mockStatic(Jsoup.class)) {
            mocked.when(() -> {
                Jsoup.connect(path);
            }).then(answer -> {
                // find local document matching path and parse to document
                String resourceName = "site/" + path;
                String html = new String(getClass().getClassLoader().getResourceAsStream(resourceName).readAllBytes());
                Document doc = Jsoup.parse(html);
                return doc;
            });

        }
    }

    @Test
    public void testScrape() throws IOException {
        scraper.start(".");
    }
}
