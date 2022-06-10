package com.Yandex.TestProject;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ShopUnitImportRequest {
    @Id
    private long id;

    public ShopUnitImportRequest() {
    }

}
