package com.Yandex.TestProject.Repositories;

import com.Yandex.TestProject.Entities.ShopUnit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ShopUnitRepository extends JpaRepository<ShopUnit, String> {
    @Query(value = "SELECT * FROM shop_unit s WHERE s.parent=?1", nativeQuery = true)
    ArrayList<ShopUnit> findAllByParent(String parent);

    @Query(value = """
            SELECT AVG(price) FROM (WITH RECURSIVE
            cte AS ( SELECT *
                     FROM shop_unit s
                     WHERE s.parent = ?1
                   UNION ALL
                     SELECT shop_unit.*
                     FROM cte
                     JOIN shop_unit ON cte.id = shop_unit.parent )
            SELECT price
            FROM cte WHERE cte.price >= 0 AND cte.type ='OFFER') as t
            """, nativeQuery = true)
    Long computeAveragePriceInCategory(String parent);

    @Query(value = """
            SELECT T2.*
            FROM (
               SELECT
                   @r AS _id,
                   (SELECT @r \\:= parent FROM shop_unit WHERE id = _id) AS parent,
                   @l \\:= @l + 1 AS lvl
               FROM
                   (SELECT @r \\:= ?1 @l \\:= 0 ) vars,shop_unit
               WHERE @r != 0) T1
            JOIN shop_unit T2
            ON T1._id = T2.id
            ORDER BY T1.lvl DESC
            """, nativeQuery = true)
    ArrayList<ShopUnit> updateDateForAllParents(String id);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE shop_unit s
            SET date= ?2\\:\\:timestamp without time zone
            WHERE s.date < ?2\\:\\:timestamp without time zone AND
            s.id IN (WITH RECURSIVE dates(id,parent,\"date\")AS(
                     SELECT s1.id,s1.parent,s1.date FROM shop_unit s1 WHERE
                             id= ?1 UNION SELECT s2.id,s2.parent,s2.date
                             FROM shop_unit s2,dates s1 WHERE s1.parent = s2.id)SELECT id
            FROM dates)""", nativeQuery = true)
    int updateDateForAllParents(String id, String date);

    @Query(value =
            """
                    SELECT
                    * 
                    FROM shop_unit s 
                    WHERE ?1 >= (s.date - INTERVAL '1' DAY)\\:\\:text
                     AND ?1 <= (s.date + INTERVAL '1' DAY)\\:\\:text
                     """
            , nativeQuery = true)
    ArrayList<ShopUnit> findSales(String date);

    ArrayList<ShopUnit> findAllByParent(ShopUnit parent);
}
