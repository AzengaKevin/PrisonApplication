package org.epics.data.repositories;

import org.epics.data.entities.User;
import org.epics.data.enums.Role;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRepository {

    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<User> findById(Integer id) {

        User user = entityManager.find(User.class, id);

        if (user != null) return Optional.of(user);

        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {

        try {

            User user = entityManager.createNamedQuery("User.findByUsername", User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            if (user != null) return Optional.of(user);

        } catch (Exception exception) {

            Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, exception.getLocalizedMessage());

        }

        return Optional.empty();
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
     * Get users of the specified roles from the database
     *
     * @param roles list of roles to examine
     * @return List of Users with that role
     */
    public List<User> findByRoles(List<Role> roles) {

        return entityManager.createNamedQuery("User.findByRoles", User.class)
                .setParameter("roles", roles)
                .getResultList();
    }

    /**
     * Updates or inserts a User in to the database
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
