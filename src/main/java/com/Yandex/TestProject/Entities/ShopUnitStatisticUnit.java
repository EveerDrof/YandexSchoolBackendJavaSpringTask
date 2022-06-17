package com.Yandex.TestProject.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ShopUnitStatisticUnit extends ShopUnitTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    private String id;

    public ShopUnitStatisticUnit() {
    }

    public ShopUnitStatisticUnit(ShopUnit shopUnit) {
        super(shopUnit.name, shopUnit.type, shopUnit.parent, shopUnit.price, shopUnit.date);
        this.id = shopUnit.getId();
    }

    public Long getPk() {
        return pk;
    }

    public String getId() {
        return id;
    }
}
