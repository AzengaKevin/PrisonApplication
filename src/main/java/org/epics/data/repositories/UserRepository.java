package org.epics.data.repositories;

import org.epics.data.entities.UserEntity;
import org.epics.data.enums.Role;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<UserEntity> findById(Integer id) {

        UserEntity userEntity = entityManager.find(UserEntity.class, id);

        if (userEntity != null) return Optional.of(userEntity);

        return Optional.empty();
    }


    /**
     * Retrieve all users from the database
     *
     * @return List<User></User> of user in the database
     */
    public List<UserEntity> findAll() {
        return entityManager.createQuery("from UserEntity", UserEntity.class).getResultList();
    }

    /**
     * Updates or inserts a User in to the database
     *
     * @param userEntity the user to save or update
     * @return a curated User updated or created
     */
    public Optional<UserEntity> save(UserEntity userEntity) throws Exception {

        entityManager.getTransaction().begin();
        entityManager.persist(userEntity);
        entityManager.getTransaction().commit();

        return Optional.of(userEntity);
    }
}
