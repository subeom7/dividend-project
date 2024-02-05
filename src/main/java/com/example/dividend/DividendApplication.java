package com.example.dividend;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.lang.annotation.Documented;

@SpringBootApplication
public class DividendApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(DividendApplication.class, args);

        Connection connection = Jsoup.connect("https://finance.yahoo.com/quote/KO/history?period1=-252374400&period2" +
                "=1707091200&interval=1mo&filter=history&frequency=1mo&includeAdjustedClose=true");
        Document document = connection.get();
        Elements eles = document.getElementsByAttributeValue("data-test", "historical-prices");
        Element ele = eles.get(0);

        Element tbody = ele.children().get(1);
        for(Element e : tbody.children()){
            String txt = e.text();
            if(!txt.endsWith("Dividend")){
                continue;
            }
            String[] splits = txt.split(" ");
            String month = splits[0];
            int day = Integer.valueOf(splits[1].replace(",", ""));
            int year = Integer.valueOf(splits[2]);
            String dividend = splits[3];

            System.out.println(year + "/" + month + "/" + day + " -> " + dividend);
        }

    }

}
