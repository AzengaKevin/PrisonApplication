package org.epics.data.entities;

import org.epics.data.enums.Gender;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "visitors")
public class Visitor extends UserEntity {

    private Gender gender;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "huduma_number")
    private String hudumaNumber;

    @Column(unique = true)
    private String phone;

    @OneToOne
    private InmateEntity inmateEntity;


    @Override
    public String getGroup() {
        return "Visitor";
    }

    public Visitor() {
    }

    public Visitor(String name, Gender gender, Date date, String hudumaNumber, String phone) {
        super(name);
        this.gender = gender;
        this.date = date;
        this.hudumaNumber = hudumaNumber;
        this.phone = phone;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getHudumaNumber() {
        return hudumaNumber;
    }

    public void setHudumaNumber(String hudumaNumber) {
        this.hudumaNumber = hudumaNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public InmateEntity getInmateEntity() {
        return inmateEntity;
    }

    public void setInmateEntity(InmateEntity inmateEntity) {
        this.inmateEntity = inmateEntity;
    }
}
