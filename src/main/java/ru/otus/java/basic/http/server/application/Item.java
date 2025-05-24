package ru.otus.java.basic.http.server.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;

public class Item {
    private static final Logger logger = LogManager.getLogger(Item.class);
    private Long id;
    private String title;
    private BigDecimal price;
    private Integer weight;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getWeight() {return weight;}

    public void setWeight(Integer weight) {this.weight = weight;}

    public Item() {
    }

    public Item(Long id, String title, BigDecimal price, Integer weight) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.weight = weight;
    }

    public void info() {
        logger.info("{}Item Info:", System.lineSeparator());
        logger.info("Идентификатор обновляемого продукта: {}", this.id);
        logger.info("Название обновляемого продукта: {}", this.title);
        logger.info("Цена обновляемого продукта: {}", this.price);
        logger.info("Вес обновляемого продукта: {}",  this.weight);
    }
}
