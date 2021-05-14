package org.epics.data;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Datasource {

    private final EntityManager entityManager;

    private Datasource() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("PrisonMainUnit");
        entityManager = entityManagerFactory.createEntityManager();
    }

    private static Datasource _instance;

    public static Datasource getInstance() {

        if (_instance == null) {
            _instance = new Datasource();
        }
        return _instance;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
