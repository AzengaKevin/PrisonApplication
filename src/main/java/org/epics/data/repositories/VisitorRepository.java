package org.epics.data.repositories;

import org.epics.data.entities.Visitor;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class VisitorRepository {

    private final EntityManager entityManager;

    public VisitorRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Visitor> findById(Integer id) throws Exception {

        Visitor visitor = entityManager.find(Visitor.class, id);

        if (visitor != null) return Optional.of(visitor);

        return Optional.empty();

    }

    public List<Visitor> findAll() throws Exception {
        return entityManager.createQuery("from Visitor", Visitor.class).getResultList();
    }

    /**
     * Updates or inserts a Visitor in to the database
     *
     * @param visitor the user to save or update
     * @return a curated User updated or created
     */
    public Optional<Visitor> save(Visitor visitor) throws Exception {

        entityManager.getTransaction().begin();
        entityManager.persist(visitor);
        entityManager.getTransaction().commit();

        return Optional.of(visitor);
    }
}
