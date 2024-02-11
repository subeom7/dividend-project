package com.example.dividend.scheduler;

import com.example.dividend.constants.CacheKey;
import com.example.dividend.domain.CompanyEntity;
import com.example.dividend.domain.DividendEntity;
import com.example.dividend.dto.Company;
import com.example.dividend.dto.ScrapedResult;
import com.example.dividend.repository.CompanyRepository;
import com.example.dividend.repository.DividendRepository;
import com.example.dividend.scraper.Scraper;
import com.example.dividend.scraper.YahooFinanceScraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
@EnableCaching
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;

//    @Scheduled(cron="0/5 * * * * *")
//    public void test() {
//        System.out.println("now -> " + System.currentTimeMillis());
//    }

    //일정 주기마다 수행
    @CacheEvict(value= CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron="${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() throws IOException {

        //저장된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();
        //회사마다 배당금 정보를 새로 스크래핑
        for(CompanyEntity company : companies){
            log.info("scraping scheduler has started -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(
                    new Company(company.getTicker(), company.getName()));

            //스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapedResult.getDividends().stream()
                    //디비든 모델을 디비든 엔티티로 매칭
                    .map(e -> new DividendEntity(company.getId(), e))
                    //엘리먼트를 하나씩 디비든 레퍼지토리에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if(!exists){
                            this.dividendRepository.save(e);
                            log.info("insert new dividend ->" + e.toString());
                        }
                    });

            //연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
