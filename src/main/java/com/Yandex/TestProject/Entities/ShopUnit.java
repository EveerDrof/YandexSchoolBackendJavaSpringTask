package com.Yandex.TestProject.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ShopUnit {
    private String name;
    private long price;
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    private ShopUnitType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    private ShopUnit parent;
    @Id
    private String id;

    public ShopUnit() {

    }

    public ShopUnit(String id, String name, ShopUnitType type, ShopUnit parent, long price, LocalDateTime date) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.price = price;
        this.date = date;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ShopUnitType getType() {
        return type;
    }

    public void setType(ShopUnitType type) {
        this.type = type;
    }

    public ShopUnit getParent() {
        return parent;
    }

    public void setParent(ShopUnit parent) {
        this.parent = parent;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime localDatetime) {
        this.date = date;
    }
}
