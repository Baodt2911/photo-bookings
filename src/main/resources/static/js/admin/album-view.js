/**
 * Album View JavaScript
 * Handles photo modal, background image setting, and other interactions
 */

document.addEventListener("DOMContentLoaded", function () {
  // Initialize album view functionality
  initializeAlbumView();
});

// Global variables
let albumMenuOpen = false;
let activeFilters = {}; // Object to store active filter types
let currentPhoto = null; // Current photo element in modal

/**
 * Initialize all album view functionality
 */
function initializeAlbumView() {
  setBackgroundImages();
  initializePhotoModal();
  initializePhotoGrid();
  initializeAlbumActions();
  initializeAlbumMenu();
  initializePhotoFilters();
  initializePhotoStatus(); // Load photo status from server
  initializeDownloadModal();
  initializeShareModal();

  // Set default filter to empty (show all by default like client)
  activeFilters = {};

  // Initialize filter UI after a short delay to ensure photos are loaded
  setTimeout(() => {
    updatePhotoStats();
    renderFilterUI();
  }, 500);
}

/**
 * Initialize download modal functionality
 */
function initializeDownloadModal() {
  const downloadModal = document.getElementById("downloadModal");
  const closeDownloadModal = document.getElementById("closeDownloadModal");
  const downloadButton = document.getElementById("downloadButton");

  if (closeDownloadModal) {
    closeDownloadModal.addEventListener("click", function () {
      closeDownloadModalFunc();
    });
  }

  if (downloadButton) {
    downloadButton.addEventListener("click", handleDownload);
  }

  // Close modal when clicking outside
  if (downloadModal) {
    downloadModal.addEventListener("click", function (e) {
      if (e.target === downloadModal) {
        closeDownloadModalFunc();
      }
    });
  }
}

/**
 * Open download modal
 */
function openDownloadModal() {
  const downloadModal = document.getElementById("downloadModal");
  if (downloadModal) {
    downloadModal.classList.remove("hidden");
  }
}

/**
 * Close download modal
 */
function closeDownloadModalFunc() {
  const downloadModal = document.getElementById("downloadModal");
  if (downloadModal) {
    downloadModal.classList.add("hidden");
  }
}

/**
 * Handle download button click
 */
function handleDownload() {
  const albumId = document.body.dataset.albumId;
  const selectedOption = document.querySelector(
    'input[name="downloadOption"]:checked'
  );

  if (!selectedOption) {
    showNotification("Vui lòng chọn tùy chọn tải xuống", "error");
    return;
  }

  const filter = selectedOption.value;

  // Show loading state
  const downloadButton = document.getElementById("downloadButton");
  const originalText = downloadButton.innerHTML;
  downloadButton.innerHTML =
    '<i class="fas fa-spinner fa-spin"></i><span>Đang tải...</span>';
  downloadButton.disabled = true;

  // Create download URL
  const downloadUrl = `/admin/albums/${albumId}/download?filter=${filter}`;

  // Use fetch to handle the download properly
  fetch(downloadUrl)
    .then((response) => {
      const contentType = response.headers.get("content-type");

      // Check if response is JSON (empty photos or error)
      if (contentType && contentType.includes("application/json")) {
        return response.json().then((data) => {
          if (data.isEmpty) {
            // Empty photos - pass the server message
            console.log("No photos found for filter:", data.error);
            throw new Error("EMPTY_PHOTOS:" + data.error);
          } else {
            // Real error
            throw new Error(data.error || "Unknown error");
          }
        });
      }

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      // Check if response is actually a zip file
      if (!contentType || !contentType.includes("application/zip")) {
        throw new Error("Response is not a zip file");
      }

      return response.blob();
    })
    .then((blob) => {
      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `album_${filter}.zip`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      // Reset button state
      downloadButton.innerHTML = originalText;
      downloadButton.disabled = false;
      closeDownloadModalFunc();
    })
    .catch((error) => {
      console.error("Download error:", error);
      downloadButton.innerHTML = originalText;
      downloadButton.disabled = false;

      // Show appropriate message
      if (error.message.startsWith("EMPTY_PHOTOS:")) {
        // Show warning notification with server message
        const serverMessage = error.message.replace("EMPTY_PHOTOS:", "");
        showNotification(serverMessage, "warning");
        closeDownloadModalFunc(); // Close modal when showing warning
      } else {
        showNotification("Lỗi khi tải xuống: " + error.message, "error");
      }
    });
}

/**
 * Set background images from data attributes
 */
function setBackgroundImages() {
  document.querySelectorAll("[data-bg-image]").forEach(function (element) {
    const bgImage = element.getAttribute("data-bg-image");
    if (bgImage) {
      element.style.backgroundImage = "url(" + bgImage + ")";
    }
  });
}

/**
 * Initialize photo modal functionality
 */
function initializePhotoModal() {
  const modal = document.getElementById("photoModal");
  const modalImage = document.getElementById("modalImage");
  const closeModal = document.getElementById("closeModal");

  if (!modal || !modalImage || !closeModal) {
    console.warn("Photo modal elements not found");
    return;
  }

  // Open modal when clicking on photos (excluding selection clicks)
  document.querySelectorAll("[data-photo-id]").forEach((item) => {
    // Add click handler to the photo item itself for viewing
    item.addEventListener("click", function (event) {
      // Check if clicking on the view button or the photo itself
      if (event.target.closest(".view-button") || event.target === item) {
        event.stopPropagation(); // Prevent other handlers from firing
        openPhotoModal(item);
      }
    });
  });

  // Close modal
  closeModal.addEventListener("click", function () {
    closePhotoModal();
  });

  // Close modal when clicking outside
  modal.addEventListener("click", function (e) {
    if (e.target === modal) {
      closePhotoModal();
    }
  });

  // Close modal with Escape key
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
      closePhotoModal();
    }
  });
}

/**
 * Open photo modal
 * @param {HTMLElement} photoElement - The photo element that was clicked
 */
async function openPhotoModal(photoElement) {
  const modal = document.getElementById("photoModal");
  if (modal) {
    currentPhoto = photoElement;
    updateModalContent();

    // Load and display comments
    await loadPhotoComments();

    modal.classList.remove("hidden");
  }
}

/**
 * Load and display comments for current photo (admin)
 */
async function loadPhotoComments() {
  if (!currentPhoto) return;

  try {
    const albumId = document.body.dataset.albumId;
    const photoId = currentPhoto.dataset.photoId;

    const response = await fetch(
      `/admin/albums/${albumId}/photos/${photoId}/comments`
    );
    const result = await response.json();

    const modalComment = document.getElementById("modalComment");
    const modalCommentText = document.getElementById("modalCommentText");

    if (result.success && result.comments && result.comments.length > 0) {
      // Show comment with the latest comment
      const latestComment = result.comments[result.comments.length - 1];
      if (modalCommentText) {
        modalCommentText.textContent = latestComment.comment;
      }
      if (modalComment) {
        modalComment.style.display = "block";
      }
      console.log(
        `Loaded comment for photo ${photoId}:`,
        latestComment.comment
      );
    } else {
      // Hide comment if no comments
      if (modalComment) {
        modalComment.style.display = "none";
      }
      console.log(`No comments found for photo ${photoId}`);
    }
  } catch (error) {
    console.error("Error loading comments:", error);
    // Hide comment on error
    const modalComment = document.getElementById("modalComment");
    if (modalComment) {
      modalComment.style.display = "none";
    }
  }
}

/**
 * Close photo modal
 */
function closePhotoModal() {
  const modal = document.getElementById("photoModal");
  if (modal) {
    modal.classList.add("hidden");
  }
}

/**
 * Initialize photo grid interactions
 */
function initializePhotoGrid() {
  // Add hover effects to photo items
  document.querySelectorAll("[data-photo-id]").forEach((item) => {
    item.addEventListener("mouseenter", function () {
      this.style.transform = "scale(1.05)";
    });

    item.addEventListener("mouseleave", function () {
      this.style.transform = "scale(1)";
    });
  });
}

/**
 * Initialize album action buttons
 */
function initializeAlbumActions() {
  // Share button
  const shareBtn = document.querySelector('[data-action="share"]');
  if (shareBtn) {
    shareBtn.addEventListener("click", function () {
      const albumId = document.body.dataset.albumId;
      shareAlbum(albumId);
    });
  }

  // Edit button
  const editBtn = document.querySelector('[data-action="edit"]');
  if (editBtn) {
    editBtn.addEventListener("click", function () {
      const albumId = document.body.dataset.albumId;
      editAlbum(albumId);
    });
  }

  // Delete button
  const deleteBtn = document.querySelector('[data-action="delete"]');
  if (deleteBtn) {
    deleteBtn.addEventListener("click", function () {
      const albumId = document.body.dataset.albumId;
      deleteAlbum(albumId);
    });
  }
}

/**
 * Share album functionality
 */
function shareAlbum() {
  if (navigator.share) {
    navigator
      .share({
        title: document.querySelector("h1").textContent,
        text: "Xem album ảnh này",
        url: window.location.href,
      })
      .catch(console.error);
  } else {
    // Fallback: copy to clipboard
    navigator.clipboard
      .writeText(window.location.href)
      .then(() => {
        showNotification("Đã sao chép link album vào clipboard", "success");
      })
      .catch(() => {
        showNotification("Không thể chia sẻ album", "error");
      });
  }
}

/**
 * Edit album functionality
 */
function editAlbum() {
  const albumId = getAlbumIdFromUrl();
  if (albumId) {
    window.location.href = `/admin/albums/${albumId}/edit`;
  }
}

/**
 * Delete album functionality
 */
function deleteAlbum() {
  const albumId = getAlbumIdFromUrl();
  if (albumId) {
    if (confirm("Bạn có chắc chắn muốn xóa album này?")) {
      fetch(`/admin/albums/${albumId}`, {
        method: "DELETE",
      })
        .then((response) => {
          if (response.ok) {
            showNotification("Album đã được xóa thành công", "success");
            setTimeout(() => {
              window.location.href = "/admin/albums";
            }, 1500);
          } else {
            throw new Error("Lỗi khi xóa album");
          }
        })
        .catch((error) => {
          console.error("Error deleting album:", error);
          showNotification("Lỗi khi xóa album", "error");
        });
    }
  }
}

/**
 * Get album ID from current URL
 */
function getAlbumIdFromUrl() {
  const path = window.location.pathname;
  const matches = path.match(/\/admin\/albums\/([^\/]+)/);
  return matches ? matches[1] : null;
}

/**
 * Show notification (if available)
 */
function showNotification(message, type = "info") {
  if (typeof window.showNotification === "function") {
    window.showNotification(message, type);
  } else {
    console.log(`${type.toUpperCase()}: ${message}`);
  }
}

/**
 * Initialize album menu functionality
 */
function initializeAlbumMenu() {
  // Close menu when clicking outside
  document.addEventListener("click", function (event) {
    const menu = document.getElementById("albumMenu");
    const button = event.target.closest('[onclick="toggleAlbumMenu()"]');

    if (!button && !menu.contains(event.target)) {
      closeAlbumMenu();
    }
  });
}

/**
 * Toggle album menu
 */
function toggleAlbumMenu() {
  const menu = document.getElementById("albumMenu");
  if (menu) {
    menu.classList.toggle("hidden");
    albumMenuOpen = !albumMenuOpen;
  }
}

/**
 * Close album menu
 */
function closeAlbumMenu() {
  const menu = document.getElementById("albumMenu");
  if (menu) {
    menu.classList.add("hidden");
    albumMenuOpen = false;
  }
}

/**
 * Initialize photo filter functionality
 */
function initializePhotoFilters() {
  // Use event delegation for filter buttons
  document.addEventListener("click", function (e) {
    if (e.target.closest("[data-type]")) {
      const filterBtn = e.target.closest("[data-type]");
      const filterType = filterBtn.getAttribute("data-type");
      handleFilterClick(filterType);
    }
  });
}

/**
 * Handle filter button click
 * @param {string} filterType - The type of filter clicked
 */
function handleFilterClick(filterType) {
  console.log(`Admin filter clicked: ${filterType}`, {
    currentFilters: activeFilters,
    filterType: filterType,
  });

  if (filterType === "all") {
    // Reset all filters when clicking "all"
    activeFilters = {};
    document.querySelectorAll("[data-type]").forEach((btn) => {
      btn.classList.remove("active");
    });
  } else {
    // Toggle specific filter
    if (activeFilters[filterType]) {
      // Remove filter
      delete activeFilters[filterType];
    } else {
      // Add filter
      activeFilters[filterType] = true;
    }

    // Update button state
    const clickedBtn = document.querySelector(`[data-type="${filterType}"]`);
    if (clickedBtn) {
      clickedBtn.classList.toggle("active");
    }

    // Remove "all" active state if any specific filter is active
    if (Object.keys(activeFilters).length > 0) {
      const allBtn = document.querySelector(`[data-type="all"]`);
      if (allBtn) {
        allBtn.classList.remove("active");
      }
    }
  }

  console.log(`Admin filters after click:`, activeFilters);
  renderFilterUI();
}

/**
 * Render filter UI based on active filters
 */
function renderFilterUI() {
  // Update filter button states
  const filterButtons = document.querySelectorAll("[data-type]");

  filterButtons.forEach((button) => {
    const filterType = button.getAttribute("data-type");
    const isActive = activeFilters[filterType];

    // Special case for "all" button
    const isAllActive = Object.keys(activeFilters).length === 0;

    // Reset classes
    button.classList.remove("bg-blue-500", "text-white", "hover:bg-gray-50");

    const icon = button.querySelector("i");
    const text = button.querySelector("span");

    if (icon) {
      icon.classList.remove("text-white", "text-gray-700");
    }
    if (text) {
      text.classList.remove("text-white", "text-gray-900");
    }

    if (isActive || (filterType === "all" && isAllActive)) {
      // Set active state
      button.classList.add("bg-blue-500", "text-white");

      if (icon) {
        icon.classList.add("text-white");
      }
      if (text) {
        text.classList.add("text-white");
      }
    } else {
      // Set inactive state
      button.classList.add("hover:bg-gray-50");

      if (icon) {
        icon.classList.add("text-gray-700");
      }
      if (text) {
        text.classList.add("text-gray-900");
      }
    }
  });

  // Apply photo filtering based on active filters
  applyPhotoFilters();
}

/**
 * Apply photo filters based on active filters
 */
function applyPhotoFilters() {
  const photos = document.querySelectorAll("[data-photo-id]");

  photos.forEach((photo) => {
    let shouldShow = true;

    // If no filters active, show all photos
    if (Object.keys(activeFilters).length === 0) {
      shouldShow = true;
    } else {
      // Check each active filter (AND logic like client)
      for (const filterType of Object.keys(activeFilters)) {
        if (activeFilters[filterType]) {
          switch (filterType) {
            case "favorites":
              if (photo.dataset.favorite !== "true") {
                shouldShow = false;
              }
              break;
            case "selected":
              if (photo.dataset.selected !== "true") {
                shouldShow = false;
              }
              break;
            case "comments":
              console.log(
                `Checking comment filter for photo ${photo.dataset.photoId}:`,
                {
                  commented: photo.dataset.commented,
                  shouldShow: photo.dataset.commented === "true",
                }
              );
              if (photo.dataset.commented !== "true") {
                shouldShow = false;
              }
              break;
          }
        }
      }
    }

    // Show/hide photo
    if (shouldShow) {
      photo.style.display = "block";
      photo.classList.remove("hidden");
    } else {
      photo.style.display = "none";
      photo.classList.add("hidden");
    }
  });
}

/**
 * Toggle select status for a photo (admin can toggle)
 * @param {HTMLElement} button - The select button element
 */
function toggleSelect(button) {
  const photo = button.closest("[data-photo-id]");
  const photoId = photo.dataset.photoId;
  const albumId = document.body.dataset.albumId;
  const isSelected = photo.dataset.selected === "true";

  // Call API to toggle select status in database
  fetch(`/admin/albums/${albumId}/photos/${photoId}/select`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        // Update local state based on server response
        const newSelectedStatus = data.isSelected;
        photo.dataset.selected = newSelectedStatus.toString();

        // Update button icon
        const icon = button.querySelector("i");
        if (newSelectedStatus) {
          icon.className = "fas fa-star text-sm";
          button.classList.remove("text-yellow-500");
          button.classList.add("text-yellow-600");
        } else {
          icon.className = "far fa-star text-sm";
          button.classList.remove("text-yellow-600");
          button.classList.add("text-yellow-500");
        }

        // Update photo stats
        updatePhotoStats();

        // Show notification
        const message = newSelectedStatus ? "Đã chọn ảnh" : "Đã bỏ chọn";
        showNotification(message, "success");
      } else {
        showNotification("Lỗi khi cập nhật trạng thái", "error");
      }
    })
    .catch((error) => {
      console.error("Error toggling select:", error);
      showNotification("Lỗi kết nối", "error");
    });
}

/**
 * Initialize photo status for all photos (load from database)
 */
function initializePhotoStatus() {
  const photos = document.querySelectorAll("[data-photo-id]");
  let loadedCount = 0;
  const totalPhotos = photos.length;

  if (totalPhotos === 0) {
    updatePhotoStats();
    return;
  }

  photos.forEach((photo) => {
    loadPhotoStatus(photo, () => {
      loadedCount++;
      if (loadedCount === totalPhotos) {
        updatePhotoStats();
      }
    });
  });
}

/**
 * Load photo status from database
 * @param {HTMLElement} photo - The photo element
 * @param {Function} callback - Callback function when status is loaded
 */
function loadPhotoStatus(photo, callback) {
  const photoId = photo.dataset.photoId;
  const albumId = document.body.dataset.albumId;

  // Call API to get photo status
  fetch(`/admin/albums/${albumId}/photos/${photoId}/status`)
    .then((response) => response.json())
    .then((data) => {
      // Update UI to show status from database
      photo.dataset.selected = data.isSelected.toString();
      photo.dataset.favorite = data.isFavorite.toString();
      photo.dataset.commented = data.hasComment.toString();

      console.log(`Admin loaded status for photo ${photoId}:`, {
        isSelected: data.isSelected,
        isFavorite: data.isFavorite,
        hasComment: data.hasComment,
        datasetCommented: photo.dataset.commented,
      });

      // Update button icons to show status
      const favoriteStatus = photo.querySelector(".favorite-status");
      const selectBtn = photo.querySelector(".select-btn");

      if (favoriteStatus) {
        const icon = favoriteStatus.querySelector("i");
        if (data.isFavorite) {
          icon.className = "fas fa-heart text-sm text-red-600";
          favoriteStatus.classList.remove("text-red-500");
          favoriteStatus.classList.add("text-red-600");
        } else {
          icon.className = "far fa-heart text-sm text-red-500";
          favoriteStatus.classList.remove("text-red-600");
          favoriteStatus.classList.add("text-red-500");
        }
      }

      if (selectBtn) {
        const icon = selectBtn.querySelector("i");
        if (data.isSelected) {
          icon.className = "fas fa-star text-sm";
          selectBtn.classList.remove("text-yellow-500");
          selectBtn.classList.add("text-yellow-600");
        } else {
          icon.className = "far fa-star text-sm";
          selectBtn.classList.remove("text-yellow-600");
          selectBtn.classList.add("text-yellow-500");
        }
      }

      // Call callback if provided
      if (callback) callback();
    })
    .catch((error) => {
      console.log("Error loading photo status:", error);
      // Call callback even on error to continue
      if (callback) callback();
    });
}

/**
 * Update photo statistics
 */
function updatePhotoStats() {
  const photos = document.querySelectorAll("[data-photo-id]");

  let favoritesCount = 0;
  let selectedCount = 0;
  let commentsCount = 0;

  console.log(`Admin updating stats for ${photos.length} photos`);

  photos.forEach((photo) => {
    if (photo.dataset.favorite === "true") favoritesCount++;
    if (photo.dataset.selected === "true") selectedCount++;
    if (photo.dataset.commented === "true") commentsCount++;

    console.log(`Admin photo ${photo.dataset.photoId}:`, {
      favorite: photo.dataset.favorite,
      selected: photo.dataset.selected,
      commented: photo.dataset.commented,
    });
  });

  console.log(
    `Admin final stats: ${favoritesCount} favorites, ${selectedCount} selected, ${commentsCount} comments`
  );

  // Update counters
  const favoritesCounter = document.getElementById("favorites-count");
  const selectedCounter = document.getElementById("selected-count");
  const commentsCounter = document.getElementById("comments-count");

  if (favoritesCounter) favoritesCounter.textContent = favoritesCount;
  if (selectedCounter) selectedCounter.textContent = selectedCount;
  if (commentsCounter) commentsCounter.textContent = commentsCount;

  console.log(`Admin updated counters:`, {
    favoritesElement: favoritesCounter,
    selectedElement: selectedCounter,
    commentsElement: commentsCounter,
  });

  // Apply filters after updating stats
  applyPhotoFilters();
}

/**
 * Update modal content
 */
function updateModalContent() {
  if (!currentPhoto) return;

  const modalImage = document.getElementById("modalImage");

  if (modalImage && currentPhoto) {
    // Update image
    const bgImage = currentPhoto.style.backgroundImage;
    const imageUrl = bgImage.replace('url("', "").replace('")', "");
    modalImage.src = imageUrl;

    // Update button states
    updateModalButtonStates();
  }
}

/**
 * Update modal button states based on photo data
 */
function updateModalButtonStates() {
  if (!currentPhoto) return;

  const selectBtn = document.getElementById("modalSelectBtn");
  const favoriteStatus = document.getElementById("modalFavoriteStatus");

  if (selectBtn) {
    const icon = selectBtn.querySelector("i");
    const isSelected = currentPhoto.dataset.selected === "true";
    icon.className = isSelected
      ? "fas fa-star text-yellow-600"
      : "far fa-star text-yellow-500";
  }

  if (favoriteStatus) {
    const icon = favoriteStatus.querySelector("i");
    const isFavorite = currentPhoto.dataset.favorite === "true";
    icon.className = isFavorite
      ? "fas fa-heart text-red-600"
      : "far fa-heart text-red-500";
  }
}

/**
 * Set current photo as cover photo
 */
function setAsCoverPhoto() {
  if (!currentPhoto) return;

  const photoId = currentPhoto.dataset.photoId;

  if (!photoId) {
    showNotification("Không thể xác định ảnh", "error");
    return;
  }

  // Get album ID from body data attribute
  const albumId = document.body.dataset.albumId;

  if (!albumId) {
    showNotification("Không thể xác định album", "error");
    return;
  }

  // Show loading state
  const setCoverBtn = document.getElementById("modalSetCoverBtn");
  if (setCoverBtn) {
    setCoverBtn.disabled = true;
    setCoverBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
  }

  // Send request to set cover photo
  const url = `/admin/albums/${albumId}/set-cover`;
  console.log("Setting cover photo URL:", url);
  console.log("Request body:", { photoId: photoId });

  fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      photoId: photoId,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      showNotification("Đã đặt làm ảnh bìa thành công!", "success");

      // Update cover photo in the UI
      updateCoverPhoto(photoId);

      // Close modal
      closePhotoModal();
    })
    .catch((error) => {
      console.error("Error setting cover photo:", error);
      showNotification("Lỗi khi đặt làm ảnh bìa: " + error.message, "error");
    })
    .finally(() => {
      // Reset button state
      if (setCoverBtn) {
        setCoverBtn.disabled = false;
        setCoverBtn.innerHTML = '<i class="fas fa-image"></i>';
      }
    });
}

/**
 * Update cover photo in the UI
 * @param {string} photoId - The photo ID to set as cover
 */
function updateCoverPhoto(photoId) {
  // Find the photo element
  const photoElement = document.querySelector(`[data-photo-id="${photoId}"]`);
  if (!photoElement) return;

  // Get the background image URL
  const bgImage = photoElement.style.backgroundImage;
  if (!bgImage) return;

  // Update the album cover section
  const albumCover = document.querySelector(".aspect-video");
  if (albumCover) {
    albumCover.style.backgroundImage = bgImage;
  }

  // Show success animation
  if (albumCover) {
    albumCover.style.transform = "scale(1.02)";
    setTimeout(() => {
      albumCover.style.transform = "scale(1)";
    }, 200);
  }
}

/**
 * Initialize Share Modal
 */
function initializeShareModal() {
  const shareModal = document.getElementById("shareModal");
  const closeShareModal = document.getElementById("closeShareModal");
  const copyCustomerSelectionLink = document.getElementById(
    "copyCustomerSelectionLink"
  );
  const copyViewerLink = document.getElementById("copyViewerLink");
  const downloadCustomerQR = document.getElementById("downloadCustomerQR");
  const downloadViewerQR = document.getElementById("downloadViewerQR");

  // Close modal handlers
  closeShareModal.addEventListener("click", closeShareModalFunc);
  shareModal.addEventListener("click", (e) => {
    if (e.target === shareModal) {
      closeShareModalFunc();
    }
  });

  // Copy link handlers
  copyCustomerSelectionLink.addEventListener("click", () => {
    const linkInput = document.getElementById("customerSelectionLink");
    linkInput.select();
    linkInput.setSelectionRange(0, 99999); // For mobile devices

    // Try modern clipboard API first
    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard
        .writeText(linkInput.value)
        .then(() => {
          showNotification("Đã sao chép đường dẫn!", "success");
        })
        .catch(() => {
          // Fallback to execCommand
          document.execCommand("copy");
          showNotification("Đã sao chép đường dẫn!", "success");
        });
    } else {
      // Fallback for older browsers
      document.execCommand("copy");
      showNotification("Đã sao chép đường dẫn!", "success");
    }
  });

  copyViewerLink.addEventListener("click", () => {
    const linkInput = document.getElementById("viewerLink");
    linkInput.select();
    linkInput.setSelectionRange(0, 99999); // For mobile devices

    // Try modern clipboard API first
    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard
        .writeText(linkInput.value)
        .then(() => {
          showNotification("Đã sao chép đường dẫn!", "success");
        })
        .catch(() => {
          // Fallback to execCommand
          document.execCommand("copy");
          showNotification("Đã sao chép đường dẫn!", "success");
        });
    } else {
      // Fallback for older browsers
      document.execCommand("copy");
      showNotification("Đã sao chép đường dẫn!", "success");
    }
  });

  // Download QR handlers
  downloadCustomerQR.addEventListener("click", () => {
    const albumId = getAlbumIdFromUrl();
    if (!albumId) {
      showNotification("Không thể lấy ID album", "error");
      return;
    }

    const baseUrl = window.location.origin;
    const customerSelectionLink = `${baseUrl}/album/${albumId}`;

    // Generate QR code URL using a free QR service
    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(
      customerSelectionLink
    )}`;

    // Create download link
    const link = document.createElement("a");
    link.href = qrUrl;
    link.download = `customer-selection-qr-${albumId}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showNotification("Đang tải QR code...", "info");
  });

  downloadViewerQR.addEventListener("click", () => {
    const albumId = getAlbumIdFromUrl();
    if (!albumId) {
      showNotification("Không thể lấy ID album", "error");
      return;
    }

    const baseUrl = window.location.origin;
    const viewerLink = `${baseUrl}/show/album/${albumId}`;

    // Generate QR code URL using a free QR service
    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(
      viewerLink
    )}`;

    // Create download link
    const link = document.createElement("a");
    link.href = qrUrl;
    link.download = `viewer-qr-${albumId}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showNotification("Đang tải QR code...", "info");
  });
}

/**
 * Open Share Modal
 */
function openShareModal() {
  const shareModal = document.getElementById("shareModal");

  // Get current album ID from URL
  const albumId = getAlbumIdFromUrl();
  if (!albumId) {
    showNotification("Không thể lấy ID album", "error");
    return;
  }

  // Generate share links
  const baseUrl = window.location.origin;
  const customerSelectionLink = `${baseUrl}/album/${albumId}`;
  const viewerLink = `${baseUrl}/show/album/${albumId}`;

  // Update input values
  const customerLinkInput = document.getElementById("customerSelectionLink");
  const viewerLinkInput = document.getElementById("viewerLink");

  if (customerLinkInput) {
    customerLinkInput.value = customerSelectionLink;
  }
  if (viewerLinkInput) {
    viewerLinkInput.value = viewerLink;
  }

  // Update password status
  updatePasswordStatus(albumId);

  // Show modal
  shareModal.classList.remove("hidden");
  shareModal.classList.add("flex");
}

/**
 * Close Share Modal
 */
function closeShareModalFunc() {
  const shareModal = document.getElementById("shareModal");
  shareModal.classList.add("hidden");
  shareModal.classList.remove("flex");
}

/**
 * Update password status in share modal
 */
function updatePasswordStatus(albumId) {
  const passwordStatusDiv = document.getElementById("passwordStatus");
  if (!passwordStatusDiv) return;

  // Get album data from window.albumData
  if (window.albumData) {
    const hasPassword = window.albumData.isPassword;

    console.log("Album data:", window.albumData);
    console.log("Has password:", hasPassword);

    // Update UI
    if (hasPassword) {
      passwordStatusDiv.innerHTML = `
        <i class="fas fa-check-circle text-green-600"></i>
        <span class="text-sm text-green-600 font-medium">Đã bảo vệ bằng mật khẩu</span>
      `;
    } else {
      passwordStatusDiv.innerHTML = `
        <i class="fas fa-times-circle text-gray-400"></i>
        <span class="text-sm text-gray-500">Chưa có mật khẩu</span>
      `;
    }
    return;
  }

  // Fallback: Check if password input exists and has value
  const passwordInput = document.querySelector('input[name="password"]');
  if (passwordInput) {
    const hasPassword =
      passwordInput.value && passwordInput.value.trim() !== "";

    if (hasPassword) {
      passwordStatusDiv.innerHTML = `
        <i class="fas fa-check-circle text-green-600"></i>
        <span class="text-sm text-green-600 font-medium">Đã bảo vệ bằng mật khẩu</span>
      `;
    } else {
      passwordStatusDiv.innerHTML = `
        <i class="fas fa-times-circle text-gray-400"></i>
        <span class="text-sm text-gray-500">Chưa có mật khẩu</span>
      `;
    }
  } else {
    // Default state
    passwordStatusDiv.innerHTML = `
      <i class="fas fa-exclamation-triangle text-yellow-500"></i>
      <span class="text-sm text-yellow-600">Không thể kiểm tra</span>
    `;
  }
}

/**
 * Submit comment for current photo
 */
async function submitComment() {
  if (!currentPhoto) return;

  const commentText = document.getElementById("commentText");
  if (!commentText) return;

  const content = commentText.value.trim();
  if (!content) {
    showNotification("Vui lòng nhập bình luận", "warning");
    return;
  }

  try {
    const response = await fetch(`/api/photos/${currentPhoto.id}/comment`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        content: content,
      }),
    });

    const result = await response.json();

    if (result.success) {
      // Update photo element in DOM
      const photoElement = document.querySelector(
        `[data-photo-id="${currentPhoto.id}"]`
      );
      if (photoElement) {
        photoElement.dataset.commented = "true";
      }

      currentPhoto.hasComment = true;
      updatePhotoUI(currentPhoto.id);
      closeCommentModal();

      // Reload comments in modal
      await loadPhotoComments();

      showNotification(result.message || "Đã thêm bình luận", "success");
    } else {
      showNotification(result.message || "Lỗi thêm bình luận", "error");
    }
  } catch (error) {
    showNotification("Lỗi thêm bình luận", "error");
  }
}

/**
 * Close comment modal
 */
function closeCommentModal() {
  const commentModal = document.getElementById("commentModal");
  if (commentModal) {
    commentModal.classList.add("hidden");
  }
  const commentText = document.getElementById("commentText");
  if (commentText) {
    commentText.value = "";
  }
}
