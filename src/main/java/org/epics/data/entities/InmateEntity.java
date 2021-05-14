package org.epics.data.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "inmates")
public class InmateEntity extends UserEntity {

    @Column(name = "case_number")
    private String caseNumber;

    private Integer gender;

    private String address;

    @Column(name = "dob")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "conviction_date")
    @Temporal(TemporalType.DATE)
    private Date convictionDate;

    @Column(name = "release_date")
    @Temporal(TemporalType.DATE)
    private Date releaseDate;

    public InmateEntity() {
    }

    public InmateEntity(String name, String caseNumber, Integer gender, String address, Date dateOfBirth, Date convictionDate, Date releaseDate) {
        super(name);
        this.caseNumber = caseNumber;
        this.gender = gender;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.convictionDate = convictionDate;
        this.releaseDate = releaseDate;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Date getConvictionDate() {
        return convictionDate;
    }

    public void setConvictionDate(Date convictionDate) {
        this.convictionDate = convictionDate;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return String.format("InmateEntity(%d, %s, %s)", id, name, caseNumber);
    }
}
