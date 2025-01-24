package com.fc.moduleapplication.controller;

import com.fc.moduleapplication.service.LowestPriceService;
import com.fc.moduleapplication.vo.Product;
import com.fc.moduleapplication.vo.ProductGroup;
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
     * <h5>Redis 상품 데이터 조회</h5>
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
     * <h5>Redis 상품 데이터 추가 및 추가된 상품 랭킹 반환</h5>
     * <pre>
     *      ProductGroupId, ProductId, Price 정보를 추가한다.
     *      ProductGroupId, ProductId 기준 Price의 순위를 조회하여 반환한다.
     *
     *      PUT 방식: 멱등성 - 없으면 새로 추가 있으면 갱신
     *      ZSET의 중복을 허용하지 않는 특성으로 인해 PUT 방식을 사용한다.
     *      (동일한 MEMBER가 이미 있다면, 해당 MEMBER에 새로운 SCORE를 덮어씌운다.)
     *      PUT 방식의 멱등성 자바의 Map 자료구조의 put의 의미와 같다고 생각하자.
     *      (Map도 key가 있다면 해당 key에 새로운 value를 덮어씌운다.)
     * </pre>
     * @param product
     * @return int ()
     */
    @PutMapping("/product")
    public int setNewProduct(@RequestBody Product product) {
        return lowestPriceService.setNewProduct(product);
    }

    @PutMapping("/productGroup")
    public int setNewProductGroup(@RequestBody ProductGroup productGroup) {
        return lowestPriceService.setNewProductGroup(productGroup);
    }

    @PutMapping("/keyword")
    public int setNewProductGroupToKeyword(String keyword, String productGroupId, int scroe) {
        return lowestPriceService.setNewProductGroupToKeyword(keyword, productGroupId, scroe);
    }
}
