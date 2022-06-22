package com.Yandex.TestProject.Repositories;

import com.Yandex.TestProject.Entities.ShopUnit;
import com.Yandex.TestProject.Entities.ShopUnitStatisticUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;

public interface ShopUnitStatisticUnitRepository extends JpaRepository<ShopUnitStatisticUnit, Long> {
    @Query(value = "SELECT * FROM shop_unit_statistic_unit s WHERE s.id = ?1 AND s.date >= ?2 AND s.date <= ?3",
            nativeQuery = true)
    ArrayList<ShopUnitStatisticUnit> findAllByIdAndPeriod(ShopUnit id, String dateStart, String dateEnd);
}
