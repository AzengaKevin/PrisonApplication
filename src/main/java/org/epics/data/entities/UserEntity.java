package org.epics.data.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class UserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(updatable = false, nullable = false)
    protected Integer id;

    @Column(nullable = false)
    protected String name;

    public UserEntity() {
    }

    public UserEntity(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("User(%d, %s)", id, name);
    }

    public abstract String getGroup();
}
