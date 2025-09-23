package vn.baodt2911.photobooking.photobooking.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @ColumnDefault("'user'")
    @Column(name = "role")
    private String role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;


    @OneToMany(mappedBy = "createdBy")
    private Set<Album> albums = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Booking> bookings = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<MyAlbum> myAlbums = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<MyPhoto> myPhotos = new LinkedHashSet<>();

    @OneToMany(mappedBy = "createdBy")
    private Set<Package> packages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<PhotoComment> photoComments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<PhotoMark> photoMarks = new LinkedHashSet<>();

}