package com.Yandex.TestProject.Repositories;

import com.Yandex.TestProject.Entities.ShopUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ShopUnitRepository extends JpaRepository<ShopUnit, String> {
    @Query(value = "SELECT * FROM shop_unit s WHERE s.parent=?1", nativeQuery = true)
    ArrayList<ShopUnit> findAllByParent(String parent);
}
