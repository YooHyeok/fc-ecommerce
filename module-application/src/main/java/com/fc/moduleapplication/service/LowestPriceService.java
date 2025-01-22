package com.fc.moduleapplication.service;

import com.fc.moduleapplication.vo.Product;
import com.fc.moduleapplication.vo.ProductGroup;

import java.util.Set;

public interface LowestPriceService {
    Set getZsetValue(String key);

    int setNewProduct(Product product);

    int setNewProductGroup(ProductGroup productGroup);
}
