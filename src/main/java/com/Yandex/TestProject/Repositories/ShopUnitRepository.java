package com.Yandex.TestProject.Repositories;

import com.Yandex.TestProject.Entities.ShopUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopUnitRepository extends JpaRepository<ShopUnit, String> {
}
