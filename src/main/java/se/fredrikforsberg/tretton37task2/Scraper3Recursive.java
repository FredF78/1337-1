package se.fredrikforsberg.tretton37task2;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Scraper3Recursive {

    private LinkedList<String> anchorLinkedList = new LinkedList<>();
    @Autowired
    private Parser parser;
    private ConcurrentHashMap<String, Boolean> visitedPages = new ConcurrentHashMap();
    private Set<String> setOfVisitedPages = visitedPages.newKeySet();
    private String rootName = "result";
    private String baseUrl = "http://books.toscrape.com";

    public void start(String baseUrl) throws IOException {
        this.baseUrl = baseUrl;
        if (Files.exists(Paths.get(rootName))) {
            FileSystemUtils.deleteRecursively(Paths.get(rootName));
        }
        Files.createDirectory(Paths.get(rootName));
        visitPage(baseUrl);
    }

    private String visitPage(String path) throws IOException {
        Document doc;

        if (!setOfVisitedPages.contains(path)) {
            log.debug("visiting link {}", path);
            doc = Jsoup.connect(path).timeout(60 * 1000).get();
            savePage(path, doc);

            List<String> anchorLinkedList = getLinksToVisitFromPage(doc);
            doc = null; // garbage collect

            // this will spawn new threads for all links found on a page and the execution will be waited upon in the join call at line 61
            // the threadpool is based on ForkJoinPool in Java
            List<CompletableFuture<String>> tasks = anchorLinkedList.stream().map(link -> CompletableFuture.supplyAsync(() -> {
                try {
                    // TODO: make all links absolute here. important to keep current path and combine with relative path in link
                    URL absoluteURL = getAbsoluteURL(path, link);
                    return visitPage(absoluteURL.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })).collect(Collectors.toList());

            List<String> result = tasks.stream().map(CompletableFuture::join).collect(Collectors.toList());
            setOfVisitedPages.addAll(result);
        }
        return path;
    }


    /**
     * Takes a JSOUP document and returns all anchors href values found in the document
     * @param doc
     * @return list of href values
     */
    private List<String> getLinksToVisitFromPage(Document doc) {
        Elements anchors = doc.select("a");
        log.debug("Found {} links", anchors.size());
        return anchors.stream().map(anchor -> anchor.attr("href")).collect(Collectors.toList());
    }

    /**
     * Takes a JSOUP document and writes the content as a string to a file on the local disk at the path specified in parameter path
     * @param path
     * @param doc
     * @throws IOException
     */
    private void savePage(String path, Document doc) throws IOException {
        log.debug("About to save path {}", rootName + File.separator + path);
        if (!Files.exists(Paths.get(parser.getPathNoFilenameFromURL(rootName + File.separator + path)))) {
            log.debug("Creating dir {}", parser.getPathNoFilenameFromURL(rootName + File.separator + path));
            Files.createDirectories(Paths.get(parser.getPathNoFilenameFromURL(rootName + File.separator + path)));
        }
        Path pathToFile = Paths.get(rootName + File.separator + parser.getPathNoHost(path));
        log.info("####################Writing page {}", pathToFile);
        try {
            Files.write(pathToFile, doc.toString().getBytes());
        } catch (IOException e) {
            log.error("File already exists. continuing {}", e.getMessage());
        }
    }

    /**
     * Creates and returns an absolute URL from two paths where the second parameter can be an relative path to the first parameter.
     * @param path
     * @param relativePath
     * @return Absolute URL
     * @throws MalformedURLException
     */
    private URL getAbsoluteURL(String path, String relativePath) throws MalformedURLException {
        URL currentPath = new URL(path);
        URL absoluteURL = new URL(currentPath, relativePath);
        return absoluteURL;
    }
}
