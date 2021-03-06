package org.epics.data.entities;

import org.epics.data.enums.Gender;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "inmates")
@NamedQueries({
        @NamedQuery(
                name = "InmateEntity.findByName",
                query = "SELECT i FROM InmateEntity i WHERE i.name = :name"
        )
})
public class InmateEntity extends UserEntity {

    @Column(name = "case_number")
    private String caseNumber;

    private Gender gender;

    private String address;

    private String block;

    private String cell;

    @Column(name = "dob")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "conviction_date")
    @Temporal(TemporalType.DATE)
    private Date convictionDate;

    @Column(name = "release_date")
    @Temporal(TemporalType.DATE)
    private Date releaseDate;

    @ManyToMany(mappedBy = "inmateEntityList")
    private Set<Task> tasks = new HashSet<>();

    @OneToOne(mappedBy = "inmateEntity")
    private Visitor visitor;

    public InmateEntity() {
    }

    public InmateEntity(String name, String caseNumber, Gender gender, String address, String block, String cell, Date dateOfBirth, Date convictionDate, Date releaseDate) {
        super(name);
        this.caseNumber = caseNumber;
        this.gender = gender;
        this.address = address;
        this.block = block;
        this.cell = cell;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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

    public Set<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    @Override
    public String toString() {
        return String.format("InmateEntity(%d, %s, %s)", id, name, caseNumber);
    }

    @Override
    public String getGroup() {
        return "Inmate";
    }
}
