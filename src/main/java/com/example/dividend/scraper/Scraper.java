package com.example.dividend.scraper;

import com.example.dividend.dto.Company;
import com.example.dividend.dto.ScrapedResult;

import java.io.IOException;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker) throws IOException;
    ScrapedResult scrap(Company company) throws IOException;
}
