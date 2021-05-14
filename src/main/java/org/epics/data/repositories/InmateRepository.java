package org.epics.data.repositories;

import org.epics.data.entities.InmateEntity;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class InmateRepository {

    private final EntityManager entityManager;

    public InmateRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<InmateEntity> findAll() {

        return entityManager.createQuery("from InmateEntity", InmateEntity.class)
                .getResultList();

    }

    /**
     * Updates or inserts a Inmate in to the database
     *
     * @param inmateEntity the user to save or update
     * @return a curated User updated or created
     */
    public Optional<InmateEntity> save(InmateEntity inmateEntity) throws Exception {

        entityManager.getTransaction().begin();
        entityManager.persist(inmateEntity);
        entityManager.getTransaction().commit();

        return Optional.of(inmateEntity);
    }
}
