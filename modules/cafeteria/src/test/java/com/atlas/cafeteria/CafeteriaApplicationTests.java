package com.atlas.cafeteria;

import com.atlas.cafeteria.entity.CafeteriaItem;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CafeteriaApplicationTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void contextLoads() {
        // Verifies:
        // - Spring context starts
        // - Flyway migrations execute
        // - Hibernate validates the migrated schema
    }

    @Test
    @Transactional
    void migratedSchemaSupportsJpaCrud() {
        CafeteriaItem item = new CafeteriaItem();
        item.setName("Test Meal");
        item.setPrice(new BigDecimal("125.50"));
        item.setCreatedAt(Instant.now());

        entityManager.persist(item);
        entityManager.flush();
        entityManager.clear();

        CafeteriaItem loaded =
                entityManager.find(CafeteriaItem.class, item.getId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getName()).isEqualTo("Test Meal");
        assertThat(loaded.getPrice())
                .isEqualByComparingTo("125.50");
        assertThat(loaded.getCreatedAt()).isNotNull();
    }

}