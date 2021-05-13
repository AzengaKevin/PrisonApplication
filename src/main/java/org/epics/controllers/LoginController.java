package org.epics.controllers;

import javafx.fxml.Initializable;
import org.epics.data.entities.User;
import org.epics.data.repositories.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    final private EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("PrisonMainUnit");
    final private EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
    final private UserRepository userRepository = new UserRepository(entityManager);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        List<User> userList = userRepository.findAll();

        userList.forEach(System.out::println);


    }
}
