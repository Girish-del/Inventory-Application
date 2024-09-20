package com.girish.product.service.api.repository;

import com.girish.product.service.api.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String > {

}
