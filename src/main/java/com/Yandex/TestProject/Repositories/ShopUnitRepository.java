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

    @Query(value = "SELECT AVG(price) FROM (WITH RECURSIVE\n" +
            "cte AS ( SELECT *\n" +
            "         FROM shop_unit s\n" +
            "         WHERE s.parent = ?1 \n" +
            "       UNION ALL\n" +
            "         SELECT shop_unit.*\n" +
            "         FROM cte\n" +
            "         JOIN shop_unit ON cte.id = shop_unit.parent )\n" +
            "SELECT price\n" +
            "FROM cte WHERE price != 0) as t", nativeQuery = true)
    Long computeAveragePriceInCategory(String parent);
}
