package org.epics.helpers;

import org.epics.data.entities.StaffEntity;

import java.io.*;
import java.util.Optional;

public class AuthHelper {

    private static final String AUTH_FILE = "auth.ser";

    public static void loginStaff(StaffEntity staffEntity) throws IOException {

        try (
                FileOutputStream fileOutputStream = new FileOutputStream(AUTH_FILE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(staffEntity);
        }

    }

    public static Optional<StaffEntity> getLoggedInStaff() throws IOException, ClassNotFoundException {

        FileInputStream fileInputStream = new FileInputStream(AUTH_FILE);

        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        StaffEntity staffEntity = (StaffEntity) objectInputStream.readObject();

        if (staffEntity != null) return Optional.of(staffEntity);

        return Optional.empty();
    }

    public static boolean logoutStaff() {
        File file = new File(AUTH_FILE);

        if (file.exists()) return file.delete();

        return false;
    }
}
