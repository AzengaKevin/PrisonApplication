package org.epics;

import org.epics.data.entities.User;
import org.epics.data.repositories.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Optional;

public class Main {

    final static private EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("PrisonMainUnit");
    final static private EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
    final static private UserRepository userRepository = new UserRepository(entityManager);

    public static void main(String[] args) {

        try {

            Optional<User> maybeAdmin = userRepository.findByUsername("testes");

            System.out.println("Test User Present: " + maybeAdmin.isPresent());

            maybeAdmin.ifPresent(System.out::println);
        } catch (Exception e) {
        }
    }
}
