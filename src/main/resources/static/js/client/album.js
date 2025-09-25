// Client Album View JavaScript
class ClientAlbumView {
  constructor() {
    this.currentPhoto = null;
    this.activeFilters = {}; // Empty filters = show all by default
    this.photos = [];
    this.albumData = null;
    this.albumMode = "view";
    this.hasPassword = false;
  }

  initialize(albumData, albumMode, hasPassword) {
    this.albumData = albumData;
    this.albumMode = albumMode;
    this.hasPassword = hasPassword;

    this.initializeEventListeners();

    // Hide interactive features if in view mode
    if (this.albumMode === "view") {
      this.hideInteractiveFeatures();
    }

    // Hide comment buttons if comments are not allowed
    if (!this.isCommentAllowed()) {
      this.hideCommentButtons();
    }

    // Check if album has password (only in select mode)
    if (this.hasPassword && this.albumMode === "select") {
      this.showPasswordModal();
    } else {
      this.loadAlbumContent();
    }
  }

  /**
   * Hide interactive features when in view mode
   */
  hideInteractiveFeatures() {
    // Hide favorite buttons
    document.querySelectorAll(".favorite-btn").forEach((btn) => {
      btn.style.display = "none";
    });

    // Hide comment buttons
    document.querySelectorAll(".comment-btn").forEach((btn) => {
      btn.style.display = "none";
    });

    // Hide modal favorite button
    const modalFavoriteBtn = document.getElementById("modalFavoriteBtn");
    if (modalFavoriteBtn) {
      modalFavoriteBtn.style.display = "none";
    }

    // Hide modal comment button
    const modalCommentBtn = document.getElementById("modalCommentBtn");
    if (modalCommentBtn) {
      modalCommentBtn.style.display = "none";
    }

    // Remove click handlers for interactive buttons
    document.querySelectorAll(".photo-item").forEach((item) => {
      item.style.cursor = "default";
    });
  }

  /**
   * Hide comment buttons when comments are not allowed
   */
  hideCommentButtons() {
    // Hide comment buttons on photo items
    document.querySelectorAll(".comment-btn").forEach((btn) => {
      btn.style.display = "none";
    });

    // Hide modal comment button
    const modalCommentBtn = document.getElementById("modalCommentBtn");
    if (modalCommentBtn) {
      modalCommentBtn.style.display = "none";
    }
  }

  /**
   * Check if album allows comments
   */
  isCommentAllowed() {
    return this.albumData && this.albumData.allowComment === true;
  }

  initializeEventListeners() {
    // Password modal
    const passwordForm = document.getElementById("passwordForm");
    if (passwordForm) {
      passwordForm.addEventListener("submit", (e) =>
        this.handlePasswordSubmit(e)
      );
    }

    // Cancel button removed - only submit button available

    // View album button - scroll to photos
    const viewAlbumBtn = document.getElementById("viewAlbumBtn");
    if (viewAlbumBtn) {
      viewAlbumBtn.addEventListener("click", () => {
        const albumContent = document.getElementById("albumContent");
        if (albumContent) {
          albumContent.scrollIntoView({
            behavior: "smooth",
            block: "start",
          });
        }
      });
    }

    // Filter buttons - use event delegation
    document.addEventListener("click", (e) => {
      if (e.target.closest("[data-type]")) {
        const filterBtn = e.target.closest("[data-type]");
        const filterType = filterBtn.getAttribute("data-type");
        this.handleFilterClick(filterType);
      }
    });

    // Photo modal
    const closePhotoModal = document.getElementById("closeModal");
    if (closePhotoModal) {
      closePhotoModal.addEventListener("click", () => this.closePhotoModal());
    }

    const photoModal = document.getElementById("photoModal");
    if (photoModal) {
      photoModal.addEventListener("click", (e) => {
        if (e.target.id === "photoModal") this.closePhotoModal();
      });
    }

    // Photo actions (only in select mode)
    const modalFavoriteBtn = document.getElementById("modalFavoriteBtn");
    if (modalFavoriteBtn && this.albumMode === "select") {
      modalFavoriteBtn.addEventListener("click", () => this.toggleFavorite());
    }

    const modalCommentBtn = document.getElementById("modalCommentBtn");
    if (modalCommentBtn && this.isCommentAllowed()) {
      modalCommentBtn.addEventListener("click", () => this.openCommentModal());
    }

    // Comment modal
    const closeCommentModal = document.getElementById("closeCommentModal");
    if (closeCommentModal) {
      closeCommentModal.addEventListener("click", () =>
        this.closeCommentModal()
      );
    }

    const cancelComment = document.getElementById("cancelComment");
    if (cancelComment) {
      cancelComment.addEventListener("click", () => this.closeCommentModal());
    }

    const submitComment = document.getElementById("submitComment");
    if (submitComment) {
      submitComment.addEventListener("click", () => this.submitComment());
    }

    // Event delegation for photo items and buttons
    document.addEventListener("click", (e) => {
      // Photo item click (but not filter buttons)
      if (e.target.closest(".photo-item") && !e.target.closest("[data-type]")) {
        const photoItem = e.target.closest(".photo-item");
        const photoId = photoItem.dataset.photoId;
        if (photoId) {
          this.openPhotoModal(photoId);
        }
      }

      // Favorite button click (only in select mode)
      if (e.target.closest(".favorite-btn") && this.albumMode === "select") {
        e.stopPropagation();
        const photoItem = e.target.closest(".photo-item");
        const photoId = photoItem.dataset.photoId;
        if (photoId) {
          this.togglePhotoFavorite(photoId);
        }
      }

      // Comment button click (only if comments allowed)
      if (e.target.closest(".comment-btn")) {
        if (this.isCommentAllowed()) {
          e.stopPropagation();
          const photoItem = e.target.closest(".photo-item");
          const photoId = photoItem.dataset.photoId;
          if (photoId) {
            this.openPhotoComment(photoId);
          }
        }
      }
    });
  }

  showPasswordModal() {
    const passwordModal = document.getElementById("passwordModal");
    if (passwordModal) {
      passwordModal.classList.remove("hidden");
    }
  }

  hidePasswordModal() {
    const passwordModal = document.getElementById("passwordModal");
    if (passwordModal) {
      passwordModal.classList.add("hidden");
    }
  }

  async handlePasswordSubmit(e) {
    e.preventDefault();
    const password = document.getElementById("albumPassword").value;

    try {
      const response = await fetch(
        `/api/albums/${this.albumData.id}/verify-password`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ password }),
        }
      );

      const result = await response.json();

      if (result.success) {
        this.hidePasswordModal();
        // Load photos after successful password verification
        await this.loadPhotosAfterVerification();
        this.loadAlbumContent();
      } else {
        showNotification(result.message || "Mật khẩu không đúng", "error");
      }
    } catch (error) {
      showNotification("Lỗi xác thực mật khẩu", "error");
    }
  }

  /**
   * Load photos after password verification
   */
  async loadPhotosAfterVerification() {
    try {
      console.log("Loading photos after verification...");
      const response = await fetch(`/api/albums/${this.albumData.id}/photos`);
      const result = await response.json();
      console.log("Photos response:", result);

      if (result.success && result.photos) {
        console.log(`Found ${result.photos.length} photos to update`);
        // Update photo elements with thumbnail URLs and status
        result.photos.forEach((photo) => {
          const photoElement = document.querySelector(
            `[data-photo-id="${photo.id}"]`
          );
          console.log(
            `Updating photo ${photo.id}:`,
            photoElement,
            photo.thumbnailUrl
          );
          if (photoElement && photo.thumbnailUrl) {
            // Update data attributes
            photoElement.dataset.bgImage = photo.thumbnailUrl;
            photoElement.dataset.favorite = photo.isFavorite.toString();
            photoElement.dataset.selected = photo.isSelected.toString();
            photoElement.dataset.commented = photo.hasComment.toString();

            // Set background image
            photoElement.style.backgroundImage = `url(${photo.thumbnailUrl})`;

            // Hide placeholder content
            const placeholder = photoElement.querySelector(
              ".placeholder-content"
            );
            if (placeholder) {
              placeholder.style.display = "none";
              console.log(`Hidden placeholder for photo ${photo.id}`);
            }

            // Show action buttons
            const actionButtons = photoElement.querySelector(".action-buttons");
            if (actionButtons) {
              actionButtons.style.display = "flex";
              console.log(`Showed action buttons for photo ${photo.id}`);
            }

            // Update button icons to show status
            const favoriteBtn = photoElement.querySelector(".favorite-btn");
            const commentBtn = photoElement.querySelector(".comment-btn");

            if (favoriteBtn) {
              const icon = favoriteBtn.querySelector("i");
              if (photo.isFavorite) {
                icon.className = "fas fa-heart text-sm text-red-600";
                favoriteBtn.classList.remove("text-red-500");
                favoriteBtn.classList.add("text-red-600");
              } else {
                icon.className = "far fa-heart text-sm text-red-500";
                favoriteBtn.classList.remove("text-red-600");
                favoriteBtn.classList.add("text-red-500");
              }
            }

            if (commentBtn) {
              const icon = commentBtn.querySelector("i");
              if (photo.hasComment) {
                icon.className = "fas fa-comment text-sm text-green-600";
                commentBtn.classList.remove("text-green-500");
                commentBtn.classList.add("text-green-600");
              } else {
                icon.className = "far fa-comment text-sm text-green-500";
                commentBtn.classList.remove("text-green-600");
                commentBtn.classList.add("text-green-500");
              }
            }
          }
        });

        // Update photo stats after loading all photos
        this.updatePhotoStats();
      }
    } catch (error) {
      console.error("Error loading photos after verification:", error);
    }
  }

  async loadAlbumContent() {
    try {
      // Photos are now rendered in HTML, just initialize the UI
      await this.initializePhotoStatus();

      // Set default filter to "all" (empty filters = show all)
      this.activeFilters = {};

      // Initialize filter UI after a short delay to ensure photos are loaded
      setTimeout(() => {
        this.updatePhotoStats();
        this.renderFilterUI();
      }, 100);
    } catch (error) {
      showNotification("Lỗi tải ảnh", "error");
    }
  }

  /**
   * Initialize photo status for all photos (load from database)
   */
  async initializePhotoStatus() {
    // Set background images first
    this.setBackgroundImages();

    const photos = document.querySelectorAll("[data-photo-id]");
    let loadedCount = 0;
    const totalPhotos = photos.length;

    if (totalPhotos === 0) {
      this.updatePhotoStats();
      return;
    }

    photos.forEach((photo) => {
      this.loadPhotoStatus(photo, () => {
        loadedCount++;
        if (loadedCount === totalPhotos) {
          this.updatePhotoStats();
        }
      });
    });
  }

  /**
   * Load photo status from database
   * @param {HTMLElement} photo - The photo element
   * @param {Function} callback - Callback function when status is loaded
   */
  async loadPhotoStatus(photo, callback) {
    const photoId = photo.dataset.photoId;
    const albumId = this.albumData.id;

    try {
      const response = await fetch(
        `/api/albums/${albumId}/photos/${photoId}/status`
      );
      const data = await response.json();

      // Update UI to show status from database
      photo.dataset.selected = data.isSelected.toString();
      photo.dataset.favorite = data.isFavorite.toString();

      // Update button icons to show status
      const favoriteBtn = photo.querySelector(".favorite-btn");
      if (favoriteBtn) {
        const icon = favoriteBtn.querySelector("i");
        if (data.isFavorite) {
          icon.className = "fas fa-heart text-sm text-red-600";
          favoriteBtn.classList.remove("text-red-500");
          favoriteBtn.classList.add("text-red-600");
        } else {
          icon.className = "far fa-heart text-sm text-red-500";
          favoriteBtn.classList.remove("text-red-600");
          favoriteBtn.classList.add("text-red-500");
        }
      }

      // Call callback if provided
      if (callback) callback();
    } catch (error) {
      console.log("Error loading photo status:", error);
      // Call callback even on error to continue
      if (callback) callback();
    }
  }

  /**
   * Load photo status from database
   * @param {HTMLElement} photo - The photo element
   * @param {Function} callback - Callback function when status is loaded
   */
  async loadPhotoStatus(photo, callback) {
    const photoId = photo.dataset.photoId;
    const albumId = this.albumData.id;

    try {
      // Call API to get photo status
      const response = await fetch(
        `/api/albums/${albumId}/photos/${photoId}/status`
      );
      const data = await response.json();

      // Update UI to show status from database
      photo.dataset.selected = data.isSelected.toString();
      photo.dataset.favorite = data.isFavorite.toString();
      photo.dataset.commented = data.hasComment.toString();

      console.log(`Photo ${photoId} status:`, {
        isSelected: data.isSelected,
        isFavorite: data.isFavorite,
        hasComment: data.hasComment,
      });

      // Update button icons to show status
      const favoriteBtn = photo.querySelector(".favorite-btn");
      const commentBtn = photo.querySelector(".comment-btn");

      if (favoriteBtn) {
        const icon = favoriteBtn.querySelector("i");
        if (data.isFavorite) {
          icon.className = "fas fa-heart text-sm text-red-600";
          favoriteBtn.classList.remove("text-red-500");
          favoriteBtn.classList.add("text-red-600");
        } else {
          icon.className = "far fa-heart text-sm text-red-500";
          favoriteBtn.classList.remove("text-red-600");
          favoriteBtn.classList.add("text-red-500");
        }
      }

      if (commentBtn) {
        const icon = commentBtn.querySelector("i");
        if (data.hasComment) {
          icon.className = "fas fa-comment text-sm text-green-600";
          commentBtn.classList.remove("text-green-500");
          commentBtn.classList.add("text-green-600");
        } else {
          icon.className = "far fa-comment text-sm text-green-500";
          commentBtn.classList.remove("text-green-600");
          commentBtn.classList.add("text-green-500");
        }
      }

      // Update photo stats
      this.updatePhotoStats();

      // Call callback if provided
      if (callback) callback();
    } catch (error) {
      console.log("Error loading photo status:", error);
      // Call callback even on error to continue
      if (callback) callback();
    }
  }

  /**
   * Update photo statistics
   */
  updatePhotoStats() {
    const photos = document.querySelectorAll("[data-photo-id]");

    let favoritesCount = 0;
    let selectedCount = 0;
    let commentsCount = 0;

    console.log(`Updating stats for ${photos.length} photos`);

    photos.forEach((photo) => {
      if (photo.dataset.favorite === "true") favoritesCount++;
      if (photo.dataset.selected === "true") selectedCount++;
      if (photo.dataset.commented === "true") commentsCount++;

      console.log(`Photo ${photo.dataset.photoId}:`, {
        favorite: photo.dataset.favorite,
        selected: photo.dataset.selected,
        commented: photo.dataset.commented,
      });
    });

    console.log(
      `Final stats: ${favoritesCount} favorites, ${selectedCount} selected, ${commentsCount} comments`
    );

    // Update counters
    const favoritesCounter = document.getElementById("favorites-count");
    const selectedCounter = document.getElementById("selected-count");
    const commentsCounter = document.getElementById("comments-count");

    if (favoritesCounter) favoritesCounter.textContent = favoritesCount;
    if (selectedCounter) selectedCounter.textContent = selectedCount;
    if (commentsCounter) commentsCounter.textContent = commentsCount;

    // Apply filters after updating stats
    this.applyPhotoFilters();
  }

  /**
   * Apply photo filters based on active filters
   */
  applyPhotoFilters() {
    const photos = document.querySelectorAll("[data-photo-id]");

    photos.forEach((photo) => {
      let shouldShow = true;

      // If no filters active, show all photos
      if (Object.keys(this.activeFilters).length === 0) {
        shouldShow = true;
      } else {
        // Check each active filter
        for (const filterType of Object.keys(this.activeFilters)) {
          if (this.activeFilters[filterType]) {
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
   * Handle filter button click
   * @param {string} filterType - The type of filter clicked
   */
  handleFilterClick(filterType) {
    if (filterType === "all") {
      // Reset all filters when clicking "all"
      this.activeFilters = {};
      document.querySelectorAll("[data-type]").forEach((btn) => {
        btn.classList.remove("active");
      });
    } else {
      // Toggle specific filter
      if (this.activeFilters[filterType]) {
        // Remove filter
        delete this.activeFilters[filterType];
      } else {
        // Add filter
        this.activeFilters[filterType] = true;
      }

      // Update button state
      const clickedBtn = document.querySelector(`[data-type="${filterType}"]`);
      if (clickedBtn) {
        clickedBtn.classList.toggle("active");
      }

      // Remove "all" active state if any specific filter is active
      if (Object.keys(this.activeFilters).length > 0) {
        const allBtn = document.querySelector(`[data-type="all"]`);
        if (allBtn) {
          allBtn.classList.remove("active");
        }
      }
    }

    this.renderFilterUI();
  }

  /**
   * Render filter UI based on active filters
   */
  renderFilterUI() {
    // Update filter button states
    const filterButtons = document.querySelectorAll("[data-type]");

    filterButtons.forEach((button) => {
      const filterType = button.getAttribute("data-type");
      const isActive =
        filterType === "all"
          ? Object.keys(this.activeFilters).length === 0
          : this.activeFilters[filterType];

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

      if (isActive) {
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
    this.applyPhotoFilters();
  }

  /**
   * Set background images from data attributes (like admin)
   */
  setBackgroundImages() {
    document.querySelectorAll("[data-bg-image]").forEach(function (element) {
      const bgImage = element.getAttribute("data-bg-image");
      if (bgImage) {
        element.style.backgroundImage = "url(" + bgImage + ")";
      }
    });
  }

  async openPhotoModal(photoId) {
    // Find photo from DOM instead of this.photos array
    const photoElement = document.querySelector(`[data-photo-id="${photoId}"]`);
    if (!photoElement) return;

    this.currentPhoto = {
      id: photoId,
      thumbnailUrl: photoElement.dataset.bgImage,
      isSelected: photoElement.dataset.selected === "true",
      isFavorite: photoElement.dataset.favorite === "true",
      hasComment: photoElement.dataset.commented === "true",
    };

    const modalImage = document.getElementById("modalImage");
    if (modalImage) {
      modalImage.src = photoElement.dataset.bgImage;
      modalImage.alt = photoId;
    }

    // Load and display comments
    await this.loadPhotoComments();

    // Update modal button states
    this.updateModalButtonStates();

    // Show selected badge if photo is selected
    this.updateSelectedBadge();

    const photoModal = document.getElementById("photoModal");
    if (photoModal) {
      photoModal.classList.remove("hidden");
    }
  }

  /**
   * Load and display comments for current photo
   */
  async loadPhotoComments() {
    if (!this.currentPhoto) return;

    try {
      const response = await fetch(
        `/api/photos/${this.currentPhoto.id}/comments`
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
          `Loaded comment for photo ${this.currentPhoto.id}:`,
          latestComment.comment
        );
      } else {
        // Hide comment if no comments
        if (modalComment) {
          modalComment.style.display = "none";
        }
        console.log(`No comments found for photo ${this.currentPhoto.id}`);
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
   * Update selected badge in modal
   */
  updateSelectedBadge() {
    // Remove existing badge
    const existingBadge = document.getElementById("selectedBadge");
    if (existingBadge) {
      existingBadge.remove();
    }

    // Add badge if photo is selected
    if (this.currentPhoto && this.currentPhoto.isSelected) {
      const photoModal = document.getElementById("photoModal");
      if (photoModal) {
        const badge = document.createElement("div");
        badge.id = "selectedBadge";
        badge.className =
          "absolute top-4 right-16 z-30 bg-green-500 text-white px-3 py-1 rounded-full text-sm font-medium";
        badge.textContent = "Được đề xuất";
        photoModal.appendChild(badge);
      }
    }
  }

  /**
   * Update modal button states based on photo data (like admin)
   */
  updateModalButtonStates() {
    if (!this.currentPhoto) return;

    const favoriteBtn = document.getElementById("modalFavoriteBtn");
    const commentBtn = document.getElementById("modalCommentBtn");

    if (favoriteBtn) {
      const icon = favoriteBtn.querySelector("i");
      const isFavorite = this.currentPhoto.isFavorite;
      icon.className = isFavorite
        ? "fas fa-heart text-sm text-red-500"
        : "far fa-heart text-sm";
    }
    if (commentBtn) {
      const icon = commentBtn.querySelector("i");
      const hasComment = this.currentPhoto.hasComment;
      icon.className = hasComment
        ? "fas fa-comment text-sm text-green-500"
        : "far fa-comment text-sm";
    }
  }

  closePhotoModal() {
    const photoModal = document.getElementById("photoModal");
    if (photoModal) {
      photoModal.classList.add("hidden");
    }

    // Remove selected badge
    const selectedBadge = document.getElementById("selectedBadge");
    if (selectedBadge) {
      selectedBadge.remove();
    }

    this.currentPhoto = null;
  }

  async toggleFavorite() {
    if (!this.currentPhoto) return;

    try {
      const response = await fetch(
        `/api/photos/${this.currentPhoto.id}/favorite`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            isFavorite: !this.currentPhoto.isFavorite,
          }),
        }
      );

      if (response.ok) {
        this.currentPhoto.isFavorite = !this.currentPhoto.isFavorite;
        this.updatePhotoUI(this.currentPhoto.id);
        showNotification(
          this.currentPhoto.isFavorite
            ? "Đã thêm vào yêu thích"
            : "Đã bỏ yêu thích",
          "success"
        );
      } else {
        showNotification("Lỗi cập nhật yêu thích", "error");
      }
    } catch (error) {
      showNotification("Lỗi cập nhật yêu thích", "error");
    }
  }

  async togglePhotoFavorite(photoId) {
    const photoElement = document.querySelector(`[data-photo-id="${photoId}"]`);
    if (!photoElement) return;

    const isCurrentlyFavorite = photoElement.dataset.favorite === "true";

    try {
      const response = await fetch(`/api/photos/${photoId}/favorite`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          isFavorite: !isCurrentlyFavorite,
        }),
      });

      if (response.ok) {
        photoElement.dataset.favorite = (!isCurrentlyFavorite).toString();
        this.updatePhotoUI(photoId);
        showNotification(
          !isCurrentlyFavorite ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích",
          "success"
        );
      } else {
        showNotification("Lỗi cập nhật yêu thích", "error");
      }
    } catch (error) {
      showNotification("Lỗi cập nhật yêu thích", "error");
    }
  }

  updatePhotoUI(photoId) {
    const photoElement = document.querySelector(`[data-photo-id="${photoId}"]`);
    if (!photoElement) return;

    // Update favorite button icon
    const favoriteBtn = photoElement.querySelector(".fa-heart");
    if (favoriteBtn) {
      const isFavorite = photoElement.dataset.favorite === "true";
      favoriteBtn.className = isFavorite
        ? "fas fa-heart text-sm text-red-500"
        : "far fa-heart text-sm";
    }

    // Update comment button icon
    const commentBtn = photoElement.querySelector(".fa-comment");
    if (commentBtn) {
      const hasComment = photoElement.dataset.commented === "true";
      commentBtn.className = hasComment
        ? "fas fa-comment text-sm text-green-500"
        : "far fa-comment text-sm";
    }

    // Update modal if open
    if (this.currentPhoto && this.currentPhoto.id === photoId) {
      this.updateModalButtonStates();
    }

    // Update photo stats
    this.updatePhotoStats();
  }

  openCommentModal() {
    if (!this.currentPhoto) {
      showNotification("Không thể mở bình luận", "error");
      return;
    }

    const commentModal = document.getElementById("commentModal");
    if (commentModal) {
      commentModal.classList.remove("hidden");
    }
    const commentText = document.getElementById("commentText");
    if (commentText) {
      commentText.focus();
    }
  }

  openPhotoComment(photoId) {
    // Find photo from DOM instead of this.photos array
    const photoElement = document.querySelector(`[data-photo-id="${photoId}"]`);
    if (!photoElement) return;

    this.currentPhoto = {
      id: photoId,
      thumbnailUrl: photoElement.dataset.bgImage,
      isSelected: photoElement.dataset.selected === "true",
      isFavorite: photoElement.dataset.favorite === "true",
      hasComment: photoElement.dataset.commented === "true",
    };

    this.openCommentModal();
  }

  closeCommentModal() {
    const commentModal = document.getElementById("commentModal");
    if (commentModal) {
      commentModal.classList.add("hidden");
    }
    const commentText = document.getElementById("commentText");
    if (commentText) {
      commentText.value = "";
    }
  }

  async submitComment() {
    if (!this.currentPhoto) return;

    const commentText = document.getElementById("commentText");
    if (!commentText) return;

    const content = commentText.value.trim();
    if (!content) {
      showNotification("Vui lòng nhập bình luận", "warning");
      return;
    }

    try {
      const response = await fetch(
        `/api/photos/${this.currentPhoto.id}/comment`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            content: content,
          }),
        }
      );

      const result = await response.json();

      if (result.success) {
        // Update photo element in DOM
        const photoElement = document.querySelector(
          `[data-photo-id="${this.currentPhoto.id}"]`
        );
        if (photoElement) {
          photoElement.dataset.commented = "true";
        }

        this.currentPhoto.hasComment = true;
        this.updatePhotoUI(this.currentPhoto.id);
        this.closeCommentModal();

        // Reload comments in modal
        await this.loadPhotoComments();

        showNotification(result.message || "Đã thêm bình luận", "success");
      } else {
        showNotification(result.message || "Lỗi thêm bình luận", "error");
      }
    } catch (error) {
      showNotification("Lỗi thêm bình luận", "error");
    }
  }
}

// Global instance
const clientAlbumView = new ClientAlbumView();

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  // Get data from Thymeleaf
  const albumData = window.albumData || {};
  const albumMode = window.albumMode || "view";
  const hasPassword = window.hasPassword || false;

  clientAlbumView.initialize(albumData, albumMode, hasPassword);
});
