package rw.madeleinegroup.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "booking_reference_sequence")
public class BookingReferenceSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer year;

    @Column(nullable = false)
    private Long lastNumber;

    @Version
    private Long version;

    public BookingReferenceSequence() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Long getLastNumber() { return lastNumber; }
    public void setLastNumber(Long lastNumber) { this.lastNumber = lastNumber; }
}
