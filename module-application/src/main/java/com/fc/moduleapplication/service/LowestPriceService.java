package com.fc.moduleapplication.service;

import com.fc.moduleapplication.vo.Keyword;
import com.fc.moduleapplication.vo.Product;
import com.fc.moduleapplication.vo.ProductGroup;

import java.util.Set;

public interface LowestPriceService {
    Set getZsetValue(String key);

    int setNewProduct(Product product);

    int setNewProductGroup(ProductGroup productGroup);

    int setNewProductGroupToKeyword(String keyword, String productGroupId, double 매칭값);
}
