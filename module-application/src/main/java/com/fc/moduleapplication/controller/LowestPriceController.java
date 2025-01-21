package com.fc.moduleapplication.controller;

import com.fc.moduleapplication.service.LowestPriceService;
import com.fc.moduleapplication.vo.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 가장 낮은 price(가격) 검색 api 컨트롤러
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class LowestPriceController {

    private final LowestPriceService lowestPriceService;
    /**
     * Redis 상품 데이터 조회
     * <pre>
     *      ProductGroupId key 값을 기준으로 조회한다.
     *      0위부터 9위까지 범위로 10개의 product를 조회한다.
     *       요청 URL: http://localhost:8080/getZSETValue?key=FPG0001
     * </pre>
     * @param key productGroupId
     * @return Set 객체 (Redis로 부터 조회한 10개의 상품에 대한 목록)
     */
    @GetMapping("/getZSETValue")
    public Set getZsetValue(String key) {
        return lowestPriceService.getZsetValue(key);
    }

    /**
     * Redis 상품 데이터 추가 및 추가된 상품 랭킹 반환
     * <pre>
     *      ProductGroupId, ProductId, Price 정보를 추가한다.
     *      ProductGroupId
     * </pre>
     * @param product
     * @return int ()
     */
    @PostMapping("/product")
    public int setNewProduct(@RequestBody Product product) {
        return lowestPriceService.setNewProduct(product);
    }
}
