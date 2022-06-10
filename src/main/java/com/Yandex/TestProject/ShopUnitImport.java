package com.Yandex.TestProject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ShopUnitImport {
    @Id
    @Column(nullable = false)
    private String id;
}
