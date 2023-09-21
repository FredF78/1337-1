package se.fredrikforsberg.tretton37task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Cli implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Cli.class, args);
    }

    @Autowired
    private Scraper scraper;
    @Override
    public void run(String... args) throws Exception {
        scraper.start("http://books.toscrape.com/", "root");
    }
}
