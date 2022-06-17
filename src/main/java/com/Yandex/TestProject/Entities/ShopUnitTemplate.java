package com.Yandex.TestProject.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@MappedSuperclass
public class ShopUnitTemplate {
    protected String name;
    protected long price;
    protected LocalDateTime date;
    @Enumerated(EnumType.STRING)
    protected ShopUnitType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    protected ShopUnit parent;

    public ShopUnitTemplate() {

    }

    public ShopUnitTemplate(String name, ShopUnitType type, ShopUnit parent, long price, LocalDateTime date) {
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.price = price;
        this.date = date;
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

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getId() {
        return null;
    }
}
