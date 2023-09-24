package se.fredrikforsberg.tretton37task2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parser {

    private URL url;
    private Pattern pathPattern = Pattern.compile("(^.*[\\/\\\\])");

    private Pattern noPathPattern = Pattern.compile("^(https?://[^/]+)(?:/[^/]+)*$");

    private String baseURL = "http://books.toscrape.com";
    private String filename = "index.html";

    public String getPathFromURL(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        return url.getPath();
    }

    public List<String> getPartsFromPath(String paths) {
        List<String> parts = List.of(paths.split("/"));
        return parts.subList(1, parts.size()); // dont return empty string before first slash
    }

    public String getPathNoFilenameFromURL(String urlString) {
        /*Matcher m = pathPattern.matcher(urlString);
        if (m == null) {
            return "";
        }
        if (m.find()) {
            return m.group(1);
        }
        return "";*/
        String urlNoHost = urlString.replaceAll(baseURL, "");
        String pathNoFileName = urlNoHost.replaceAll(filename, "");
        return pathNoFileName;
    }

    public String getPathNoHost(String urlString) {
        String urlNoHost = urlString.replaceAll(baseURL, "");
        return urlNoHost;
    }

    public boolean hasPath(String urlString) {
        Matcher m = noPathPattern.matcher(urlString);
        if (m == null) {
            return false;
        }
        if (m.find()) {
            return false;
        } else {
            return true;
        }
    }
}
