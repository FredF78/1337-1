package se.fredrikforsberg.tretton37task2;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest(classes = {Scraper3Recursive.class, Parser.class})
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Slf4j
public class Scraper3RecursiveTest {


    @InjectMocks
    private Scraper3Recursive scraper;

    @BeforeEach
    public void setupMock() {
        log.info("setting up mock");
        String path = "";
        try (MockedStatic mocked = mockStatic(Jsoup.class)) {
            mocked.when(() -> {
                Jsoup.connect(anyString());
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
    @Disabled
    public void testScrape() throws IOException {
        scraper.start(".");
    }
}
