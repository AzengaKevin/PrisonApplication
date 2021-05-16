package org.epics.data.repositories;

import org.epics.data.entities.StaffEntity;
import org.epics.data.enums.Role;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class StaffRepository {

    private final EntityManager entityManager;

    public StaffRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<StaffEntity> findById(Integer id) {

        StaffEntity staffEntity = entityManager.find(StaffEntity.class, id);

        if (staffEntity != null) return Optional.of(staffEntity);

        return Optional.empty();
    }


    public Optional<StaffEntity> findByUsername(String username) throws Exception {

        StaffEntity staffEntity = entityManager.createNamedQuery("StaffEntity.findByUsername", StaffEntity.class)
                .setParameter("username", username)
                .getSingleResult();

        if (staffEntity != null) return Optional.of(staffEntity);

        return Optional.empty();
    }

    public List<StaffEntity> findAll() {

        return entityManager.createQuery("from StaffEntity", StaffEntity.class)
                .getResultList();

    }

    /**
     * Get staff of the specified roles from the database
     *
     * @param roles list of roles to examine
     * @return List of Users with that role
     */
    public List<StaffEntity> findByRoles(List<Role> roles) {

        return entityManager.createNamedQuery("StaffEntity.findByRoles", StaffEntity.class)
                .setParameter("roles", roles)
                .getResultList();
    }

    /**
     * Updates Staff in to the database
     *
     * @param userEntity the user to save or update
     * @return a curated User updated or created
     */
    public Optional<StaffEntity> update(StaffEntity userEntity) throws Exception {
        entityManager.getTransaction().begin();
        entityManager.merge(userEntity);
        entityManager.getTransaction().commit();

        return Optional.of(userEntity);
    }

    /**
     * Updates or inserts a User in to the database
     *
     * @param userEntity the user to save or update
     * @return a curated User updated or created
     */
    public Optional<StaffEntity> save(StaffEntity userEntity) throws Exception {

        entityManager.getTransaction().begin();
        entityManager.persist(userEntity);
        entityManager.getTransaction().commit();

        return Optional.of(userEntity);
    }
}
