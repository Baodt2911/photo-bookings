package vn.baodt2911.photobooking.photobooking.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.baodt2911.photobooking.photobooking.entity.Album;
import vn.baodt2911.photobooking.photobooking.repository.AlbumRepository;
import vn.baodt2911.photobooking.photobooking.service.interfaces.AlbumService;

import java.util.List;
import java.util.UUID;

@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Override
    public List<Album> findAll() {
        return albumRepository.findAll();
    }

    @Override
    public Album findById(UUID id) {
        return albumRepository.findById(id).orElse(null);
    }

    @Override
    public Album save(Album album) {
        return albumRepository.save(album);
    }

    @Override
    public void deleteById(UUID id) {
        albumRepository.deleteById(id);
    }
    
    @Override
    public Page<Album> findAlbumsWithFilters(String search, Boolean allowDownload, Boolean allowComment, UUID createdById, Pageable pageable) {
        if (search != null && !search.trim().isEmpty() && allowDownload != null && allowComment != null && createdById != null) {
            // Search with all filters
            return albumRepository.findByNameContainingIgnoreCaseAndAllowDownloadAndAllowCommentAndCreatedById(search.trim(), allowDownload, allowComment, createdById, pageable);
        } else if (search != null && !search.trim().isEmpty() && allowDownload != null && allowComment != null) {
            // Search with download and comment filters
            return albumRepository.findByNameContainingIgnoreCaseAndAllowDownloadAndAllowComment(search.trim(), allowDownload, allowComment, pageable);
        } else if (search != null && !search.trim().isEmpty() && allowDownload != null && createdById != null) {
            // Search with download and creator filters
            return albumRepository.findByNameContainingIgnoreCaseAndAllowDownloadAndCreatedById(search.trim(), allowDownload, createdById, pageable);
        } else if (search != null && !search.trim().isEmpty() && allowComment != null && createdById != null) {
            // Search with comment and creator filters
            return albumRepository.findByNameContainingIgnoreCaseAndAllowCommentAndCreatedById(search.trim(), allowComment, createdById, pageable);
        } else if (search != null && !search.trim().isEmpty() && allowDownload != null) {
            // Search with download filter
            return albumRepository.findByNameContainingIgnoreCaseAndAllowDownload(search.trim(), allowDownload, pageable);
        } else if (search != null && !search.trim().isEmpty() && allowComment != null) {
            // Search with comment filter
            return albumRepository.findByNameContainingIgnoreCaseAndAllowComment(search.trim(), allowComment, pageable);
        } else if (search != null && !search.trim().isEmpty() && createdById != null) {
            // Search with creator filter
            return albumRepository.findByNameContainingIgnoreCaseAndCreatedById(search.trim(), createdById, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            // Search only
            return albumRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else if (allowDownload != null && allowComment != null && createdById != null) {
            // Download, comment and creator filters
            return albumRepository.findByAllowDownloadAndAllowCommentAndCreatedById(allowDownload, allowComment, createdById, pageable);
        } else if (allowDownload != null && allowComment != null) {
            // Download and comment filters
            return albumRepository.findByAllowDownloadAndAllowComment(allowDownload, allowComment, pageable);
        } else if (allowDownload != null && createdById != null) {
            // Download and creator filters
            return albumRepository.findByAllowDownloadAndCreatedById(allowDownload, createdById, pageable);
        } else if (allowComment != null && createdById != null) {
            // Comment and creator filters
            return albumRepository.findByAllowCommentAndCreatedById(allowComment, createdById, pageable);
        } else if (allowDownload != null) {
            // Download filter only
            return albumRepository.findByAllowDownload(allowDownload, pageable);
        } else if (allowComment != null) {
            // Comment filter only
            return albumRepository.findByAllowComment(allowComment, pageable);
        } else if (createdById != null) {
            // Creator filter only
            return albumRepository.findByCreatedById(createdById, pageable);
        } else {
            // No filters, return all
            return albumRepository.findAll(pageable);
        }
    }
    
    @Override
    public long count() {
        return albumRepository.count();
    }
    
    @Override
    public long countByAllowDownload(boolean allowDownload) {
        return albumRepository.countByAllowDownload(allowDownload);
    }
    
    @Override
    public long countByAllowComment(boolean allowComment) {
        return albumRepository.countByAllowComment(allowComment);
    }
    
    @Override
    public long countByCreatedById(UUID createdById) {
        return albumRepository.countByCreatedById(createdById);
    }
}
