package com.Yandex.TestProject.Repositories;

import com.Yandex.TestProject.Entities.ShopUnit;
import com.Yandex.TestProject.Entities.ShopUnitStatisticUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface ShopUnitStatisticUnitRepository extends JpaRepository<ShopUnitStatisticUnit, Long> {
    @Query(value = "SELECT * FROM shop_unit_statistic_unit s WHERE s.id = ?1 AND s.date >= ?2 AND s.date <= ?3",
            nativeQuery = true)
    ArrayList<ShopUnitStatisticUnit> findAllByIdAndPeriod(ShopUnit id, String dateStart, String dateEnd);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM shop_unit_statistic_unit WHERE id=?1 OR parent=?1 OR  parent OR id IN(select  id\n" +
            " from    (select * from shop_unit\n" +
            "         order by parent, id) shop_unit,\n" +
            "        (select @pv \\:=  ?1) initialisation\n" +
            " where   find_in_set(parent, @pv) > 0\n" +
            " and     @pv \\:= concat(@pv, ',', id))", nativeQuery = true)
    Integer deleteByIdRecursive(String id);
}
