package com.Yandex.TestProject.Services;

import com.Yandex.TestProject.Entities.ShopUnit;
import com.Yandex.TestProject.Entities.ShopUnitStatisticUnit;
import com.Yandex.TestProject.Repositories.ShopUnitRepository;
import com.Yandex.TestProject.Repositories.ShopUnitStatisticUnitRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ShopUnitImportService {
    private ShopUnitRepository shopUnitRepository;
    private EntityManagerFactory entityManagerFactory;
    private DateTimeFormatter formatter;
    private ShopUnitStatisticUnitRepository shopUnitStatisticUnitRepository;

    @Autowired
    public ShopUnitImportService(ShopUnitRepository shopUnitRepository, EntityManagerFactory entityManagerFactory,
                                 ShopUnitStatisticUnitRepository shopUnitStatisticUnitRepository) {
        this.shopUnitStatisticUnitRepository = shopUnitStatisticUnitRepository;
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"
        ));
        this.shopUnitRepository = shopUnitRepository;
        this.entityManagerFactory = entityManagerFactory;
    }

    public ShopUnit save(ShopUnit shopUnit) {
        return shopUnitRepository.save(shopUnit);
    }

    public Optional<ShopUnit> findById(String id) {
        return shopUnitRepository.findById(id);
    }

    public ArrayList<ShopUnit> findAllByParentId(String id) {
        return shopUnitRepository.findAllByParent(id);
    }

    public long computeAveragePriceInCategory(String parent) {
        if (shopUnitRepository.findAllByParent(parent).size() == 0) {
            return -1;
        }
        Long result = shopUnitRepository.computeAveragePriceInCategory(parent);
        if (result != null) {
            return result;
        } else {
            return -1;
        }
    }

    public void updateDateForAllParents(String id, LocalDateTime date) {
        String formattedTime = formatter.format(date);
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("UPDATE shop_unit s SET s.`date`= '" + formattedTime + "' WHERE s.`date`" +
                " < '" + formattedTime + "' AND s.id IN (WITH RECURSIVE dates(id,parent,`date`)AS(\n" +
                "                SELECT s1.id,s1.parent,s1.`date` FROM shop_unit s1 WHERE" +
                " id= '" + id + "' UNION SELECT s2.id,s2.parent,s2.`date`\n" +
                "                FROM shop_unit s2,dates s1 WHERE s1.parent = s2.id)SELECT id " +
                " FROM dates)").executeUpdate();
        em.getTransaction().commit();
    }

    public ArrayList<ShopUnit> getSales(LocalDateTime date) {
        return shopUnitRepository.findSales(formatter.format(date));
    }

    public ArrayList<ShopUnitStatisticUnit> findStatisticsAllByIdAndPeriod(ShopUnit id, LocalDateTime dateStart,
                                                                           LocalDateTime dateEnd) {
        return shopUnitStatisticUnitRepository.findAllByIdAndPeriod(id, formatter.format(dateStart),
                formatter.format(dateEnd));
    }

    public void saveShopStatisticUnit(ShopUnitStatisticUnit shopUnitStatisticUnit) {
        shopUnitStatisticUnitRepository.save(shopUnitStatisticUnit);
    }

    public void deleteByIdRecursive(ShopUnit shopUnit) {
        System.out.println("+++++++++++++++++++0");
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createNativeQuery("""
                DELETE FROM shop_unit_statistic_unit WHERE pk IN(
                SELECT pk FROM (SELECT pk FROM shop_unit_statistic_unit WHERE id ='asdfasfd') as T
                )
                """);
        System.out.println("+++++++++++++++++++Before");
        query.executeUpdate();
        System.out.println("+++++++++++++++++++After");
//        em.createNativeQuery(String.format("""
//                WITH RECURSIVE ids(id, parent) AS (
//                    SELECT
//                        s1.id,
//                        s1.parent
//                    FROM shop_unit s1
//                    WHERE id = '%s'
//                        UNION
//                    SELECT
//                        s2.id,
//                        s2.parent
//                    FROM shop_unit s2, ids s1
//                    WHERE s2.parent = s1.id
//                )
//                delete from shop_unit_statistic_unit s
//                where s.id in (
//                    select
//                    id
//                    from ids
//                )
//                """, shopUnit.getId())).executeUpdate();
        em.getTransaction().commit();
        System.out.println("+++++++++++++++++++1");
        shopUnitRepository.findAllByParent(shopUnit).forEach((this::deleteByIdRecursive));
        System.out.println("+++++++++++++++++++2");
        shopUnitRepository.deleteById(shopUnit.getId());
        System.out.println("+++++++++++++++++++3");

    }
}
