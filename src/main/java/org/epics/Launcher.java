package org.epics;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.epics.data.Datasource;
import org.epics.data.entities.StaffEntity;
import org.epics.data.enums.Role;
import org.epics.data.repositories.StaffRepository;
import org.epics.helpers.Log;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Launcher extends Application {

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

        Task<StaffEntity> checkAdminTask = new Task<>() {
            @Override
            protected StaffEntity call() throws Exception {

                Datasource datasource = Datasource.getInstance();

                StaffRepository staffRepository = new StaffRepository(datasource.getEntityManager());

                Optional<StaffEntity> maybeAdmin = staffRepository.findByUsername(ADMIN_USERNAME);

                if (maybeAdmin.isEmpty()) {

                    maybeAdmin = staffRepository.save(new StaffEntity("Super Admin", ADMIN_USERNAME, ADMIN_PASSWORD, Role.Admin));

                }

                return maybeAdmin.orElseThrow(() -> new Exception("Fatal Error Occurred"));
            }
        };

        checkAdminTask.setOnFailed(workerEvent -> {
            Log.error(Launcher.class.getSimpleName(), "checkAndCreateAdminIfNecessary", workerEvent.getSource().getException());
            try {

                Datasource datasource = Datasource.getInstance();

                StaffRepository staffRepository = new StaffRepository(datasource.getEntityManager());
                staffRepository.save(new StaffEntity("Super Admin", ADMIN_USERNAME, ADMIN_PASSWORD, Role.Admin));

            } catch (Exception exception) {
                Log.error(Launcher.class.getSimpleName(), "checkAndCreateAdminIfNecessary", workerEvent.getSource().getException());
            }
        });

        checkAdminTask.setOnSucceeded(workerStateEvent -> {
            StaffEntity userEntity = (StaffEntity) workerStateEvent.getSource().getValue();
            Logger.getLogger(TAG).log(Level.INFO, userEntity.toString());

        });

        executor.execute(checkAdminTask);
    }
}
