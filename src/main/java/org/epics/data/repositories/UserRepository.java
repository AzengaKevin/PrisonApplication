package org.epics.data.repositories;

import org.epics.data.entities.User;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<User> findById(Integer id) {

        User user = entityManager.find(User.class, id);

        return Optional.of(user);
    }

    public Optional<User> findByUsername(String username) {

        User user = entityManager.createNamedQuery("User.findByUsername", User.class)
                .setParameter("username", username)
                .getSingleResult();

        return Optional.of(user);
    }

    /**
     * Retrieve all users from the database
     *
     * @return List<User></User> of user in the database
     */
    public List<User> findAll() {
        return entityManager.createQuery("from User", User.class).getResultList();
    }

    /**
     * Upserts a User in to the database
     *
     * @param user the user to save or update
     * @return a curated User updated or created
     */
    public Optional<User> save(User user) {
        entityManager.getTransaction().begin();
        entityManager.persist(user);
        entityManager.getTransaction().commit();

        return Optional.of(user);
    }
}
