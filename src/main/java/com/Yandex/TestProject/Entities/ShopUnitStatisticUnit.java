package com.Yandex.TestProject.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ShopUnitStatisticUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    private String name;
    private long price;
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    private ShopUnitType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    private ShopUnit parent;
    private String id;

    public ShopUnitStatisticUnit() {
    }

    public ShopUnitStatisticUnit(ShopUnit shopUnit) {
        this.id = shopUnit.getId();
        this.name = shopUnit.getName();
        this.type = shopUnit.getType();
        this.parent = shopUnit.getParent();
        this.price = shopUnit.getPrice();
        this.date = shopUnit.getDate();
    }

    public Long getPk() {
        return pk;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public ShopUnitType getType() {
        return type;
    }

    public ShopUnit getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }
}
