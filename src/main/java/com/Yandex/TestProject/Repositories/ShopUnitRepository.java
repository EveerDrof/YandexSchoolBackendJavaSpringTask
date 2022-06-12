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

    //    @Query(value =
//            " UPDATE shop_unit s SET s.`date`=?2 WHERE " +
//                    " ( !ISNULL(s.id) AND  s.id IN (SELECT T2.id  " +
//                    " FROM ( " +
//                    "    SELECT " +
//                    "        @r AS _id, " +
//                    "        (SELECT @r\\:=parent FROM shop_unit WHERE id = _id) AS parent, " +
//                    "        @l\\:=@l + 1 AS lvl " +
//                    "    FROM " +
//                    "        (SELECT @r\\:=?1, @l\\:=0) vars, " +
//                    "        shop_unit h " +
//                    "    WHERE @r <> 0) T1 " +
//                    " JOIN shop_unit T2 " +
//                    " ON T1._id = T2.id " +
//                    " ORDER BY T1.lvl DESC));", nativeQuery = true)
    @Query(value = "SELECT T2.*\n" +
            " FROM (\n" +
            "    SELECT\n" +
            "        @r AS _id,\n" +
            "        (SELECT @r \\:= parent FROM shop_unit WHERE id = _id) AS parent,\n" +
            "        @l \\:= @l + 1 AS lvl\n" +
            "    FROM\n" +
            "        (SELECT @r \\:= ?1 @l \\:= 0 ) vars,shop_unit \n" +
            "    WHERE @r != 0) T1\n" +
            " JOIN shop_unit T2\n" +
            " ON T1._id = T2.id\n" +
            " ORDER BY T1.lvl DESC;", nativeQuery = true)
    ArrayList<ShopUnit> updateDateForAllParents(String id);
}
