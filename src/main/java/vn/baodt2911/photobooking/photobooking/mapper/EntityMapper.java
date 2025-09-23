package vn.baodt2911.photobooking.photobooking.mapper;

import org.springframework.stereotype.Component;
import vn.baodt2911.photobooking.photobooking.dto.request.*;
import vn.baodt2911.photobooking.photobooking.dto.response.*;
import vn.baodt2911.photobooking.photobooking.entity.*;
import vn.baodt2911.photobooking.photobooking.entity.Package;

@Component
public class EntityMapper {

    // Package mappers
    public PackageResponseDTO toPackageResponseDTO(Package packageEntity) {
        if (packageEntity == null) return null;
        
        PackageResponseDTO dto = new PackageResponseDTO();
        dto.setId(packageEntity.getId());
        dto.setName(packageEntity.getName());
        dto.setSlug(packageEntity.getSlug());
        dto.setDescription(packageEntity.getDescription());
        dto.setPrice(packageEntity.getPrice());
        dto.setCurrency(packageEntity.getCurrency());
        dto.setDurationMinutes(packageEntity.getDurationMinutes());
        dto.setMaxPeople(packageEntity.getMaxPeople());
        dto.setIncludes(packageEntity.getIncludes());
        dto.setImageUrl(packageEntity.getImageUrl());
        dto.setActive(packageEntity.getActive());
        dto.setCreatedAt(packageEntity.getCreatedAt());
        dto.setUpdatedAt(packageEntity.getUpdatedAt());
        return dto;
    }

    public Package toPackageEntity(PackageCreateRequestDTO dto) {
        if (dto == null) return null;
        
        Package entity = new Package();
        entity.setName(dto.getName());
        entity.setSlug(dto.getSlug());
        entity.setDescription(dto.getDescription());
        entity.setIncludes(dto.getIncludes());
        entity.setPrice(dto.getPrice());
        entity.setDurationMinutes(dto.getDurationMinutes());
        entity.setMaxPeople(dto.getMaxPeople());
        entity.setActive(true); // Default to active
        entity.setCurrency("VND"); // Default currency
        return entity;
    }

    public void updatePackageEntity(Package entity, PackageUpdateRequestDTO dto) {
        if (entity == null || dto == null) return;
        
        // Don't update ID, only update other fields
        entity.setName(dto.getName());
        entity.setSlug(dto.getSlug());
        entity.setDescription(dto.getDescription());
        entity.setIncludes(dto.getIncludes());
        entity.setPrice(dto.getPrice());
        entity.setDurationMinutes(dto.getDurationMinutes());
        entity.setMaxPeople(dto.getMaxPeople());
    }

    // Booking mappers
    public BookingResponseDTO toBookingResponseDTO(Booking booking) {
        if (booking == null) return null;
        
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setUserName(booking.getUser() != null ? booking.getUser().getName() : null);
        dto.setUserEmail(booking.getUser() != null ? booking.getUser().getEmail() : null);
        dto.setPackageName(booking.getPackageField() != null ? booking.getPackageField().getName() : null);
        dto.setName(booking.getName());
        dto.setEmail(booking.getEmail());
        dto.setPhone(booking.getPhone());
        dto.setBookingPrice(booking.getBookingPrice());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        return dto;
    }

    public Booking toBookingEntity(BookingCreateRequestDTO dto) {
        if (dto == null) return null;
        
        Booking entity = new Booking();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setBookingPrice(dto.getBookingPrice());
        entity.setBookingDate(dto.getBookingDate());
        entity.setStatus(dto.getStatus());
        return entity;
    }

    public void updateBookingEntity(Booking entity, BookingUpdateRequestDTO dto) {
        if (entity == null || dto == null) return;
        
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setBookingPrice(dto.getBookingPrice());
        entity.setBookingDate(dto.getBookingDate());
        entity.setStatus(dto.getStatus());
    }

    // Album mappers
    public AlbumResponseDTO toAlbumResponseDTO(Album album) {
        if (album == null) return null;
        
        AlbumResponseDTO dto = new AlbumResponseDTO();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setDriveFolderLink(album.getDriveFolderLink());
        dto.setPassword(album.getPassword());
        dto.setAllowDownload(album.getAllowDownload());
        dto.setAllowComment(album.getAllowComment());
        dto.setLimitSelection(album.getLimitSelection());
        dto.setCreatedByName(album.getCreatedBy() != null ? album.getCreatedBy().getName() : null);
        dto.setCreatedAt(album.getCreatedAt());
        dto.setUpdatedAt(album.getUpdatedAt());
        return dto;
    }

    public Album toAlbumEntity(AlbumCreateRequestDTO dto) {
        if (dto == null) return null;
        
        Album entity = new Album();
        entity.setName(dto.getName());
        entity.setDriveFolderLink(dto.getDriveFolderLink());
        entity.setPassword(dto.getPassword());
        entity.setAllowDownload(dto.getAllowDownload());
        entity.setAllowComment(dto.getAllowComment());
        entity.setLimitSelection(dto.getLimitSelection());
        return entity;
    }

    public void updateAlbumEntity(Album entity, AlbumUpdateRequestDTO dto) {
        if (entity == null || dto == null) return;
        
        entity.setName(dto.getName());
        entity.setDriveFolderLink(dto.getDriveFolderLink());
        entity.setPassword(dto.getPassword());
        entity.setAllowDownload(dto.getAllowDownload());
        entity.setAllowComment(dto.getAllowComment());
        entity.setLimitSelection(dto.getLimitSelection());
    }

    // User mappers
    public UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) return null;
        
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    public User toUserEntity(UserCreateRequestDTO dto) {
        if (dto == null) return null;
        
        User entity = new User();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
        entity.setRole(dto.getRole());
        return entity;
    }

    public void updateUserEntity(User entity, UserUpdateRequestDTO dto) {
        if (entity == null || dto == null) return;
        
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setRole(dto.getRole());
    }

    // MyAlbum mappers
    public MyAlbumResponseDTO toMyAlbumResponseDTO(MyAlbum myAlbum) {
        if (myAlbum == null) return null;
        
        MyAlbumResponseDTO dto = new MyAlbumResponseDTO();
        dto.setId(myAlbum.getId());
        dto.setName(myAlbum.getName());
        dto.setDescription(myAlbum.getDescription());
        dto.setIsPublic(myAlbum.getIsPublic());
        dto.setUserName(myAlbum.getUser() != null ? myAlbum.getUser().getName() : null);
        dto.setCreatedAt(myAlbum.getCreatedAt());
        dto.setUpdatedAt(myAlbum.getUpdatedAt());
        dto.setPhotoCount(myAlbum.getMyPhotos() != null ? myAlbum.getMyPhotos().size() : 0);
        return dto;
    }

    public MyAlbum toMyAlbumEntity(MyAlbumCreateRequestDTO dto) {
        if (dto == null) return null;
        
        MyAlbum entity = new MyAlbum();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIsPublic(dto.getIsPublic());
        return entity;
    }

    public void updateMyAlbumEntity(MyAlbum entity, MyAlbumUpdateRequestDTO dto) {
        if (entity == null || dto == null) return;
        
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIsPublic(dto.getIsPublic());
    }

    // MyPhoto mappers
    public MyPhotoResponseDTO toMyPhotoResponseDTO(MyPhoto myPhoto) {
        if (myPhoto == null) return null;
        
        MyPhotoResponseDTO dto = new MyPhotoResponseDTO();
        dto.setId(myPhoto.getId());
        dto.setUrl(myPhoto.getUrl());
        dto.setTitle(myPhoto.getTitle());
        dto.setDescription(myPhoto.getDescription());
        dto.setOrderIndex(myPhoto.getOrderIndex());
        dto.setIsFavorite(myPhoto.getIsFavorite());
        dto.setCreatedAt(myPhoto.getCreatedAt());
        dto.setUpdatedAt(myPhoto.getUpdatedAt());
        // Note: size field is not available in MyPhoto entity, will be null
        return dto;
    }
}
