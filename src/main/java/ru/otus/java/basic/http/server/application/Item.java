package ru.otus.java.basic.http.server.application;

import java.math.BigDecimal;

public class Item {
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
}
