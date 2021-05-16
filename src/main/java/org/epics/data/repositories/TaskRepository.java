package org.epics.data.repositories;

import org.epics.data.entities.StaffEntity;
import org.epics.data.entities.Task;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class TaskRepository {

    private final EntityManager entityManager;

    public TaskRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Task> findById(Integer id) throws Exception {

        Task task = entityManager.find(Task.class, id);

        if (task != null) return Optional.of(task);

        return Optional.empty();

    }

    public Optional<Task> findByTitle(String title) throws Exception {

        Task taskEntity = entityManager.createNamedQuery("Task.findByTitle", Task.class)
                .setParameter("title", title)
                .getSingleResult();

        if (taskEntity != null) return Optional.of(taskEntity);

        return Optional.empty();
    }

    public List<Task> findAll() throws Exception {
        return entityManager.createQuery("from Task", Task.class).getResultList();
    }

    /**
     * Updates or inserts a Task in to the database
     *
     * @param task the user to save or update
     * @return a curated User updated or created
     */
    public Optional<Task> save(Task task) throws Exception {

        entityManager.getTransaction().begin();
        entityManager.persist(task);
        entityManager.getTransaction().commit();

        return Optional.of(task);
    }
}
