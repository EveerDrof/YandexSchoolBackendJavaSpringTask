package com.Yandex.TestProject.Repositories;

import com.Yandex.TestProject.Entities.ShopUnit;
import com.Yandex.TestProject.Entities.ShopUnitStatisticUnit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;

public interface ShopUnitStatisticUnitRepository extends JpaRepository<ShopUnitStatisticUnit, Long> {
    @Query(value = """
            SELECT * FROM
            shop_unit_statistic_unit s
            WHERE s.id = ?1 AND
            s.date >= ?2\\:\\:timestamp without time zone AND
            s.date <= ?3\\:\\:timestamp without time zone
            """,
            nativeQuery = true)
    ArrayList<ShopUnitStatisticUnit> findAllByIdAndPeriod(ShopUnit id, String dateStart, String dateEnd);

    @Transactional
    @Modifying
    @Query(value = """
            WITH RECURSIVE ids(id, parent) AS (
                SELECT
                    s1.id,
                    s1.parent
                FROM shop_unit s1
                WHERE id = ?1
                    UNION
                SELECT
                    s2.id,
                    s2.parent
                FROM shop_unit s2, ids s1
                WHERE s2.parent = s1.id
            )
            delete from shop_unit_statistic_unit s
            where s.id in (
                select
                id
                from ids
            )
            """, nativeQuery = true)
    void deleteStatistics(String id);
}
