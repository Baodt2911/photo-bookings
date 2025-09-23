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

@Getter
@Setter
@Entity
@Table(name = "albums")
public class Album {
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
    @Column(name = "customer_name", nullable = false)
    private String customer_name;

    @NotNull
    @Lob
    @Column(name = "drive_folder_link", nullable = false)
    private String driveFolderLink;

    @Size(max = 255)
    @Column(name = "password")
    private String password;

    @ColumnDefault("0")
    @Column(name = "allow_download")
    private Boolean allowDownload;

    @ColumnDefault("1")
    @Column(name = "allow_comment")
    private Boolean allowComment;

    @Column(name = "limit_selection")
    private Integer limitSelection;

    @Column(name = "cover_photo_id")
    private Long coverPhotoId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "album")
    private Set<Photo> photos = new LinkedHashSet<>();

}