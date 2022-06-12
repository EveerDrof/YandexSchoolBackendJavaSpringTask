package com.Yandex.TestProject.Services;

import com.Yandex.TestProject.Entities.ShopUnit;
import com.Yandex.TestProject.Repositories.ShopUnitRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
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

    @Autowired
    public ShopUnitImportService(ShopUnitRepository shopUnitRepository, EntityManagerFactory entityManagerFactory) {

        this.shopUnitRepository = shopUnitRepository;
        this.entityManagerFactory = entityManagerFactory;
    }

    public void save(ShopUnit shopUnit) {
        shopUnitRepository.save(shopUnit);
    }

    public Optional<ShopUnit> findById(String id) {
        return shopUnitRepository.findById(id);
    }

    public ArrayList<ShopUnit> findAllByParentId(String id) {
        return shopUnitRepository.findAllByParent(id);
    }

    public long computeAveragePriceInCategory(String parent) {
        Long result = shopUnitRepository.computeAveragePriceInCategory(parent);
        if (result != null) {
            return result.longValue();
        }
        return 0;
    }

    public void updateDateForAllParents(String id, LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
        String formattedTime = formatter.format(date);
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("UPDATE shop_unit s SET" +
                " s.`date`='" + formattedTime + "' WHERE " +
                "(s.`date` < '" + formattedTime + "' AND s.id IN (SELECT T2.id\n" +
                "FROM (\n" +
                "    SELECT\n" +
                "        @r AS _id,\n" +
                "        (SELECT @r \\:= parent FROM shop_unit WHERE id = _id) AS parent,\n" +
                "        @l \\:= @l + 1 AS lvl\n" +
                "    FROM\n" +
                "        (SELECT @r \\:= '" + id + "', @l \\:= 0) vars,\n" +
                "        shop_unit h\n" +
                "    WHERE @r <> 0) T1\n" +
                "JOIN shop_unit T2\n" +
                "ON T1._id = T2.id\n" +
                "ORDER BY T1.lvl DESC));").executeUpdate();
        em.getTransaction().commit();
    }
}
