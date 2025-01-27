package com.fc.moduleapplication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fc.moduleapplication.vo.Keyword;
import com.fc.moduleapplication.vo.Product;
import com.fc.moduleapplication.vo.ProductGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LowestPriceServiceImpl implements LowestPriceService {

    private final RedisTemplate redisTemplate;

    /**
     * <h1>Redis(ZSET-ZRANGE) 상품그룹,범위 기준 상품 정보 목록 조회</h1>
     * <pre>
     *      Redis ZSET(sorted set) ZRANGE 조회
     *      productGroupId(key) 기준 0~9까지 범위에 해당하는 10개의 Product(member)를 Price(score)와 함께 조회한다.
     *      Redis 조회 메소드: rangeWithScores(상품그룹Id(key), 범위시작인덱스, 범위끝인덱스)
     *      Redis 조회 명령: zrange {상품그룹Id(key)} {범위시작인덱스} {범위끝인덱스} withscores
     * </pre>
     * @param key
     * @return 상품 정보 목록
     */
    @Override
    public Set getZsetValue(String key) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.rangeWithScores(key, 0, 9); // 범위 기준 Scores(price) 조회: ZRANGE key 0 9 withscores (key, 시작범위, 끝범위)
    }

    /**
     * <h1>Redis(ZSET-ZADD,ZRANK) 추가 및 추가된 값 순위 조회</h1>
     * <pre>
     *      Redis ZSET(sorted set) ZADD 추가 및 ZRANK 조회
     *        1. productGroupId(key), productId(member), price(score) 데이터를 추가한다.
     *        2. productGroupId(key) 에서 productId(member)에 대한 순위를 조회한다.
     *      Redis ZSET 추가
     *        - Redis 추가 메소드: add(상품그룹Id(key), 상품Id(member), 가격(score))
     *        - Redis 추가 명령: zadd {상품그룹Id(key)} {가격(score)} {상품Id(member)}
     *      Redis ZSET 순위 조회
     *        - Redis 순위 조회 메소드: rank(상품그룹Id(key), 상품Id(member))
     *        - Redis 순위 조회 명령: zrank {상품그룹Id(key)} {가격(score)}}
     * </pre>
     * @param product
     * @return 순위값
     */
    @Override
    public int setNewProduct(Product product) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(product.getProductGroupId(), product.getProductId(), product.getPrice()); // key, value, score 순서로 product 정보를 추가한다.
        return zSetOperations.rank(product.getProductGroupId(), product.getProductId()).intValue(); // 추가한 상품 랭킹 조회
    }

    /**
     * <h1>상품 그룹 추가 및 추가된 상품 그룹의 상품 목록 갯수 조회</h1>
     * <pre>
     *     Redis (ZSET-ZADD,ZCARD) ZADD 추가 및 ZCARD 조회
     *       1. productGroupId(key), productId(member), price(score) 데이터를 추가한다.
     *       2. productGroupId(key) 기준 product 목록 갯수를 조회한다.
     *     Redis ZSET 추가
     *       - Redis 추가 메소드: add(상품그룹Id(key), 상품Id(member), 가격(score))
     *       - Redis 추가 명령: zadd {상품그룹Id(key)} {가격(score)} {상품Id(member)}
     *     Redis ZCARD Key별 Member 갯수 조회
     *       - Redis 순위 조회 메소드: zCard(상품그룹Id(key))
     *       - Redis 순위 조회 명령: zcard {상품그룹Id(key)}
     * </pre>
     * @param productGroup
     * @return
     */
    @Override
    public int setNewProductGroup(ProductGroup productGroup) {
        Product product = productGroup.getProductList().get(0); // 리스트에 1개의 product group 데이터만 구성하여 넘겨받으므로 첫번째 product 데이터만 추출
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(product.getProductGroupId(), product.getProductId(), product.getPrice());
        return zSetOperations.zCard(productGroup.getProductGroupId()).intValue(); // 프로덕트 그룹 key에 해당하는
    }

    /**
     * <h1>키워드 추가 및 추가된 키워드의 상품 그룹의 매칭값(score) 순위 조회</h1>
     * <pre>
     *     Redis (ZSET-ZADD,ZCARD) ZADD 추가 및 ZCARD 조회
     *       1. keyword(key), productGroupId(member), 매칭값(score) 데이터를 추가한다.
     *       2. keyword(key) 기준 product 목록 갯수를 조회한다.
     *     Redis ZSET 추가
     *       - Redis 추가 메소드: add(상품키워드(key), 상품그룹Id(member), 매칭값(score))
     *       - Redis 추가 명령: zadd {상품키워드(key)} {매칭값(score)} {상품그룹Id(member)}
     *     Redis ZSET 순위 조회
     *       - Redis 순위 조회 메소드: rank(키워드(key), 상품그룹Id(member))
     *       - Redis 순위 조회 명령: zrank {키워드(key)} {매칭값(score)}}
     * </pre>
     * @param keyword
     * @param productGroupId
     * @param scroe
     * @return
     */
    @Override
    public int setNewProductGroupToKeyword(String keyword, String productGroupId, double scroe) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(keyword, productGroupId, scroe);
        return zSetOperations.rank(keyword, productGroupId).intValue();
    }

    @Override
    public Keyword getLowestPriceProductByKeyword(String keyword) {
        List<ProductGroup> productGroupList = getProductGroupUsingKeyword(keyword);
        Keyword returnInfo = new Keyword();
        returnInfo.setProductGroupList(productGroupList);
        return returnInfo;
    }

    private List<ProductGroup> getProductGroupUsingKeyword(String keyword) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        /* keyword 기준 productGroup 10개 가격기준 내림차순 조회 */
        Set productGroupIdSet = zSetOperations.reverseRange(keyword, 0, 9); // productGroupId 목록 조회
        List<String> productGroupIdList = List.copyOf(productGroupIdSet);
        List<ProductGroup> returnInfo = new ArrayList<>();
        for (final String productGroupId : productGroupIdList) {

            /* Loop - ProductGroup 기준 Product,Price 10개 조회 */
            Set productAndPriceSet = zSetOperations.rangeWithScores(productGroupId, 0, 9);
            Iterator iterator = productAndPriceSet.iterator();

            /**
             * Loop - ProductGroupList에 ProductGroup 추가.
             * 직렬화된 값들을 Map으로 변환.
             * Product에 id, price 할당.
             * ProductGroup의 ProductList에 Product 추가
             * ProductGroupList에 ProductGroup 추가.
             */
            ProductGroup productGroup = new ProductGroup();
//            productGroup.setProductList(new ArrayList<>());
            while (iterator.hasNext()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> productPriceMap = objectMapper.convertValue(iterator.next(), Map.class);
                /* Product에 id, price 할당. */
                Product product = new Product();
                product.setProductGroupId(productGroupId);
                product.setProductId(productPriceMap.get("value").toString()); // [redis]value: productId
                product.setPrice(Double.valueOf(productPriceMap.get("score").toString()).intValue());// [redis]score: price
                /* ProductGroup의 ProductList에 Product 추가 */
                productGroup.getProductList().add(product);
            }
            productGroup.setProductGroupId(productGroupId);
            /* ProductGroupList에 ProductGroup 추가. */
            returnInfo.add(productGroup);
        }
        return returnInfo;
    }

    private List<ProductGroup> getProductGroupUsingKeywordRefactor(String keyword) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        /* keyword 기준 productGroup 10개 가격기준 내림차순 조회 */
        Set<String> productGroupIdSet = zSetOperations.reverseRange(keyword, 0, 9); // productGroupId 목록 조회
        List<ProductGroup> returnInfo = new ArrayList<>();
        for (final String productGroupId : productGroupIdSet) {

            /**
             * Loop - ProductGroupList에 ProductGroup 추가.
             * Product에 id, price 매핑.
             * ProductGroup의 ProductList에 Product 추가
             * ProductGroupList에 ProductGroup 추가.
             */
            Set<ZSetOperations.TypedTuple<Object>> productAndPriceSet = zSetOperations.rangeWithScores(productGroupId, 0, 9);
            ProductGroup productGroup = new ProductGroup();
            List<Product> productList = productAndPriceSet.stream().map(stringTypedTuple -> {
                /* Product에 id, price 할당. */
                Product product = new Product();
                product.setProductGroupId(productGroupId);
                product.setProductId((String) stringTypedTuple.getValue());
                product.setPrice(stringTypedTuple.getScore().intValue());
                return product;
            }).collect(Collectors.toList());
            /* ProductGroup의 ProductList에 Product 추가 */
            productGroup.setProductGroupId(productGroupId);
            productGroup.setProductList(productList);
            /* ProductGroupList에 ProductGroup 추가. */
            returnInfo.add(productGroup);
        }
        return returnInfo;
    }
}
