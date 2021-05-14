package org.epics;

import org.epics.data.entities.StaffEntity;
import org.epics.data.enums.Role;
import org.epics.data.repositories.StaffRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Optional;

public class Main {

    final static private EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("PrisonMainUnit");
    final static private EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
    final static private StaffRepository userRepository = new StaffRepository(entityManager);

    public static void main(String[] args) throws Exception {

        try {


            Optional<StaffEntity> maybeAdmin = userRepository.findByUsername("test");

            if (maybeAdmin.isEmpty()) {
                userRepository.save(new StaffEntity("Test User", "test", "elephant69", Role.Doctor));
            }

            maybeAdmin.ifPresent(System.out::println);

        } catch (Exception e) {

            userRepository.save(new StaffEntity("Test User", "test", "elephant69", Role.Doctor));

        }
    }
}
