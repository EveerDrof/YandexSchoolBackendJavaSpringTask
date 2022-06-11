package com.Yandex.TestProject.Entities;

import jakarta.persistence.*;

@Entity
public class ShopUnit {
    @Id
    private String id;
    private String name;
    private long price;
    @Enumerated(EnumType.STRING)
    private ShopUnitType type;
    @ManyToOne
    @JoinColumn(name = "parent")
    private ShopUnit parent;

    public ShopUnit() {

    }

    public ShopUnit(String id, String name, ShopUnitType type, ShopUnit parent) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.parent = parent;
//        this.price = price;
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
}
