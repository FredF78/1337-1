package se.fredrikforsberg.tretton37task2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("!test")
public class Cli2 implements CommandLineRunner{

    public static void main(String[] args) {
        SpringApplication.run(Cli2.class, args);
    }

    @Autowired
    private Scraper3Recursive scraper;
    @Override
    public void run(String... args) throws Exception {
        scraper.start("http://books.toscrape.com");
    }
}
