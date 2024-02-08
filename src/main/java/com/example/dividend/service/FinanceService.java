package com.example.dividend.service;

import com.example.dividend.constants.CacheKey;
import com.example.dividend.domain.CompanyEntity;
import com.example.dividend.domain.DividendEntity;
import com.example.dividend.dto.Company;
import com.example.dividend.dto.Dividend;
import com.example.dividend.dto.ScrapedResult;
import com.example.dividend.repository.CompanyRepository;
import com.example.dividend.repository.DividendRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key="#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName){
        log.info("search company ->" + companyName);
        //1. 회사명을 기준으로 회사 정보조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                                        .orElseThrow(() -> new RuntimeException("존재 하지 않는 회사명 입니다"));

        //2. 조회된 회사 id로 배당금을 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());


        //3. 결과 조합 후 반환
        // for문 사용 구현
//        List<Dividend> dividends = new ArrayList<>();
//        for(DividendEntity entity : dividendEntities){
//            dividends.add(Dividend.builder()
//                    .date(entity.getDate())
//                    .dividend(entity.getDividend())
//                    .build());
//        }

        //stream 사용구현
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(), company.getName()),
                                dividends);
    }

}
