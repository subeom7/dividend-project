package com.example.dividend.service;

import com.example.dividend.domain.CompanyEntity;
import com.example.dividend.domain.DividendEntity;
import com.example.dividend.dto.Company;
import com.example.dividend.dto.ScrapedResult;
import com.example.dividend.repository.CompanyRepository;
import com.example.dividend.repository.DividendRepository;
import com.example.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) throws IOException {
        boolean exsists = companyRepository.existsByTicker(ticker);
        if(exsists) {
            throw new RuntimeException("The ticker already exsists -> " + ticker);
        }
        return storeCompanyAndDividend(ticker);
    }

    private Company storeCompanyAndDividend(String ticker) throws IOException {
        //scarp company by ticekr
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);


        //if the company exists, scrap company information
        ScrapedResult scrapedResult = yahooFinanceScraper.scrap(company);

        //Scraping result
        CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .toList();

        this.dividendRepository.saveAll(dividendEntityList);
        return company;
    }
}
