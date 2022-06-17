package com.Yandex.TestProject.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class ShopUnit extends ShopUnitTemplate {
    @Id
    private String id;

    public ShopUnit() {

    }

    public ShopUnit(String id, String name, ShopUnitType type, ShopUnit parent, long price, LocalDateTime date) {
        super(name, type, parent, price, date);
        this.id = id;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
