package org.epics.data.entities;

import org.epics.data.enums.Role;

import javax.persistence.*;

@Entity
@Table(name = "staff")
@NamedQueries({
        @NamedQuery(
                name = "StaffEntity.findByUsername",
                query = "SELECT u FROM StaffEntity u WHERE u.username = :username"
        ),
        @NamedQuery(
                name = "StaffEntity.findByRoles",
                query = "SELECT u FROM StaffEntity u WHERE u.role IN (:roles)"
        )
})
public class StaffEntity extends UserEntity {

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    private Role role;

    public StaffEntity() {
    }

    public StaffEntity(String name, String username, String password, Role role) {
        super(name);
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return String.format(
                "Staff(%d, %s, %s, %s, %s)",
                id,
                name,
                username,
                role.getSlug(),
                password
        );
    }
}
