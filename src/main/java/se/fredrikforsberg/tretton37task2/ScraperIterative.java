package se.fredrikforsberg.tretton37task2;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScraperIterative {
    // Algoritm nedan: baseras på bredth first traversal iterativt (ej rekursivt anrop)

    // ladda ner rotsida
    // parsea rotsida
    // identifiera länkar på rotsida
    // lägg länkar på kö

    // så länge det finns länk på kön:
    //      plocka ett antal länkar
    //      för varje länk:
    //          skapa parallell task som
    //              laddar ner sida bakom länk x
    //              parsear sida
    //              identifierar lönkar på sida
    //              lägger länkar på kön


    // antingen rekursivt med länklista på stacken som funktionsanrop
    // eller en whileloop och en kö


    private LinkedList<String> anchorLinkedList = new LinkedList<>();

    @Autowired
    private Parser parser;

    private List<String> visitedPages = new ArrayList<>();

    public void start() throws IOException {
        String rootName = "result";
        String baseUrl = "http://books.toscrape.com/";
        String startUrl = baseUrl + "index.html";
        Document doc = Jsoup.connect(startUrl).timeout(60 * 1000).get();
        visitedPages.add(startUrl);
        Files.createDirectory(Paths.get(rootName));
        Files.write(Paths.get(rootName + File.separator + "index.html"), doc.toString().getBytes());
        log.info("Writing page index.html");
        Elements anchors = doc.select("a");
        this.anchorLinkedList.addAll(anchors.stream().map(anchor -> anchor.attr("href")).collect(Collectors.toList()));
        List<CompletableFuture> futures = new ArrayList<>();
        while (this.anchorLinkedList.size() > 0) {
            String linkToVisit = this.anchorLinkedList.remove();

            CompletableFuture future = CompletableFuture.supplyAsync(() -> {
                if (!visitedPages.contains(baseUrl + linkToVisit)) {
                    Document document;
                    try {
                        document = Jsoup.connect(baseUrl + linkToVisit).timeout(60 * 1000).get();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (!Files.exists(Paths.get(rootName + File.separator + parser.getPathNoFilenameFromURL(linkToVisit)))) {
                        try {
                            Files.createDirectories(Paths.get(rootName + File.separator + parser.getPathNoFilenameFromURL(linkToVisit)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        Files.write(Paths.get(rootName + File.separator + linkToVisit), doc.toString().getBytes());
                        log.info("Writing page {}", linkToVisit);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            });

            futures.add(future);
            if (futures.size() == 10) {
                futures.stream().forEach(CompletableFuture::join);
            }
        }
    }
}
