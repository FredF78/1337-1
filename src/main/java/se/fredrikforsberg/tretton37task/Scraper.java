package se.fredrikforsberg.tretton37task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class Scraper {

    private ObjectMapper om = new ObjectMapper();
    private String baseUrl;
    private int currentPage = 0;
    private Pattern pattern = Pattern.compile("^(.*/)(?!.*\\/)");

    public void start(String url, String currentWorkdir) throws IOException, ExecutionException, InterruptedException {
        this.baseUrl = url;
        visitAndScrape(url, currentWorkdir);
    }
    public void visitAndScrape(String url, String currentWorkdir) throws IOException, ExecutionException, InterruptedException {
        Document doc = Jsoup.connect(url).get();
        Elements bookLinks = doc.select("ol.row li div.image_container a[href]");
        if (bookLinks.size() > 0) {
            handleGridPage(url, currentWorkdir, doc, bookLinks);
        } else {
            try {
                handleBookPage(currentWorkdir, doc);
            } catch (URISyntaxException e) {
                log.warn("Skipping book, unexpexted parse error of some kind");
            }
        }
    }

    private void handleGridPage(String urlToScrape, String currentWorkdir, Document doc, Elements bookLinks) throws IOException, InterruptedException, ExecutionException {
        log.info("Saving books on page {}", this.currentPage);
        Path indexDirectoryPath = createPageDirectory();
        List<CompletableFuture<Book>> bookFutures = new ArrayList<>();
        for (Element bookLink : bookLinks) {
            String bookFilePath =  bookLink.attr("href");

            String fullUrl = getBookUrl(urlToScrape, bookFilePath);
            CompletableFuture<Book> bookFuture = CompletableFuture.supplyAsync(() -> {

                try {
                    visitAndScrape(fullUrl, indexDirectoryPath.toString());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (ExecutionException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                return Book.builder().title(fullUrl).build();
            });
            bookFutures.add(bookFuture);
        }
        bookFutures.stream().forEach(CompletableFuture::join);
        parseAndNagivateNext(currentWorkdir, doc);
    }

    private String getBookUrl(String url, String bookFilePath) {
        String newBookBaseUrl = baseUrl;
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            newBookBaseUrl = matcher.group(1);
        }

        String fullUrl = newBookBaseUrl + bookFilePath;
        return fullUrl;
    }

    private Path createPageDirectory() throws IOException {
        Path indexDirectoryPath = Paths.get("page" + this.currentPage);
        Files.createDirectory(indexDirectoryPath);
        return indexDirectoryPath;
    }

    private void handleBookPage(String currentWorkdir, Document doc) throws IOException, URISyntaxException, InterruptedException {
        //log.info("GOT BOOK PAGE");
        String title = getTextByQuery(doc, "div.product_main h1");
        String description = getTextByQuery(doc, "div#product_description + p");
        String imageSource = getAttributeValueByQuery(doc, "div#product_gallery div.item.active img", "src");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(this.baseUrl + imageSource.replace("../../", "")))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<byte[]> image = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        List<Map.Entry> tableMetadataEnteries = getBookTableMetadata(doc);

        writeBook(title, description, tableMetadataEnteries, image, currentWorkdir, imageSource);
        log.info("Saving book {} to folder {}", title, currentWorkdir);
    }

    private static String getAttributeValueByQuery(Document doc, String query, String attr) {
        Elements elements = doc.select(query);
        if (elements.size() > 0) {
            String attrval = elements.get(0).attr(attr);
            return attrval;
        } else {
            return "";
        }
    }

    private static String getTextByQuery(Document doc, String cssQuery) {
        Elements elements = doc.select(cssQuery);
        if (elements.size() > 0) {
            String title = elements.get(0).text();
            return title;
        } else {return "";}
    }

    private static List<Map.Entry> getBookTableMetadata(Document doc) {
        Elements metadataRows = doc.select("table.table-striped tr");
        List<Map.Entry> entries = new ArrayList<>();
        for (Element row : metadataRows) {
            String key = row.select("th").text();
            String val = row.select("td").text();
            Map.Entry entry = Map.entry(key, val);
            entries.add(entry);
        }
        return entries;
    }

    private void parseAndNagivateNext(String currentWorkdir, Document doc) throws IOException, ExecutionException, InterruptedException {
        String nextLink = getAttributeValueByQuery(doc,"ul.pager li.next a[href]", "href");
        if (null != nextLink && !nextLink.isEmpty()) {
            this.currentPage++;
            String newBaseUrl = this.baseUrl;
            if (this.currentPage > 1) {
                newBaseUrl = this.baseUrl + "catalogue/";
            }
            String nextUrl = newBaseUrl + nextLink;
            this.visitAndScrape(nextUrl, currentWorkdir);
        }
    }

    private void writeBook(String title, String description, List entries, HttpResponse<byte[]> imageResponse, String currentWorkdir, String imageFilename) throws IOException {
        Path fileDir = Path.of(getBookDirectoryName(currentWorkdir, title));
        Files.createDirectory(fileDir);
        File metadata = new File(fileDir + File.separator + "content.json");
        metadata.createNewFile();

        Book book = Book.builder().title(title).description(description).entries(entries).build();
        try{
            FileWriter fw = new FileWriter(metadata.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(om.writeValueAsString(book));
            bw.close();

            byte[] data = imageResponse.body();
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            try {
                OutputStream output = new FileOutputStream(fileDir + File.separator + makeLocalFilename(imageFilename));
                try {
                    ByteStreams.copy(is, output);
                } finally {
                    Closeables.closeQuietly(is);
                }
            } finally {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String makeLocalFilename(String imageFilename) {
        return imageFilename.replaceAll(".*/([^/]+)$", "$1");
    }

    private static String getBookDirectoryName(String currentWorkdir, String title) {
        int length = title.length() > 40 ? 40 : title.length();
        return currentWorkdir + File.separator + title.trim().substring(0, length).replaceAll("[:\\/\\s]", "");
    }
}
