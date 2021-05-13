package org.epics;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.epics.data.entities.User;
import org.epics.data.repositories.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Launcher extends Application {

    final static private EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("PrisonMainUnit");
    final static private EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
    final static private UserRepository userRepository = new UserRepository(entityManager);

    private static Executor executor;

    public static final String ADMIN_USERNAME = "admin", ADMIN_PASSWORD = "elephant69";
    private static final String TAG = "Launcher";


    @Override
    public void start(Stage stage) throws Exception {

        Parent parent = FXMLLoader.load(getClass().getResource("/layouts/Login.fxml"));

        stage.setTitle("Login | Prison Management System");
        stage.setResizable(false);

        Scene scene = new Scene(parent);
        scene.getStylesheets().addAll(this.getClass().getResource("/styles/master.css").toExternalForm());
        stage.setScene(scene);

        stage.show();

    }

    public static void main(String[] args) {

        executor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });

        checkAndCreateAdminIfNecessary();

        launch(args);
    }

    /**
     * Check if the super admin is in the database if not, create super admin
     */
    private static void checkAndCreateAdminIfNecessary() {

        Task<User> checkAdminTask = new Task<>() {
            @Override
            protected User call() throws Exception {

                Optional<User> maybeAdmin = userRepository.findByUsername(ADMIN_USERNAME);

                if (maybeAdmin.isEmpty()) {
                    maybeAdmin = userRepository.save(new User("Super Admin", ADMIN_USERNAME, ADMIN_PASSWORD, "admin"));
                }

                return maybeAdmin.orElseThrow(() -> new Exception("Fatal Error Occurred"));
            }
        };

        checkAdminTask.setOnFailed(workerEvent -> System.err.println(workerEvent.getSource().getException().getLocalizedMessage()));

        checkAdminTask.setOnSucceeded(workerStateEvent -> {
            User user = (User) workerStateEvent.getSource().getValue();
            Logger.getLogger(TAG).log(Level.INFO, user.toString());

        });

        executor.execute(checkAdminTask);
    }
}
