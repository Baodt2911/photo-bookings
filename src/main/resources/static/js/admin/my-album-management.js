// My Album Management JavaScript
let currentMyAlbumId = null;
let currentStep = 1;
let uploadedImages = [];
let selectedCoverPhotoId = null;
let selectedCoverPhotoIndex = null;

// Edit Album Functions - Using main form
let currentEditAlbumId = null;
let currentEditPhotos = [];

// Album management functions
function editMyAlbum(albumId) {
  openMyAlbumModalForEdit(albumId);
}

function deleteMyAlbum(albumId) {
  // Store albumId for confirmation
  window.myAlbumToDelete = albumId;

  // Show confirmation modal
  const confirmModal = document.getElementById("confirmModal");
  const confirmMessage = document.getElementById("confirmMessage");
  confirmMessage.textContent =
    "Bạn có chắc chắn muốn xóa album này? Hành động này không thể hoàn tác.";

  confirmModal.classList.remove("hidden");
}

function shareMyAlbum(albumId) {
  // TODO: Implement share functionality
  console.log("Share album:", albumId);
  showNotification("Tính năng chia sẻ đang được phát triển", "info");
}

// Initialize page
document.addEventListener("DOMContentLoaded", function () {
  setupModalClickOutside();
  setupDragAndDrop();
  setupSearchAndFilter();
});

// Step management functions
function nextStep() {
  if (currentStep === 1) {
    // Validate step 1
    const name = document.getElementById("myAlbumName").value.trim();
    if (!name) {
      showNotification("Vui lòng nhập tên album", "error");
      return;
    }

    // Move to step 2
    currentStep = 2;
    updateStepIndicators();
    showStep(2);
  }
}

function prevStep() {
  if (currentStep === 2) {
    currentStep = 1;
    updateStepIndicators();
    showStep(1);
  }
}

function updateStepIndicators() {
  const step1Indicator = document.getElementById("step1Indicator");
  const step2Indicator = document.getElementById("step2Indicator");

  if (currentStep === 1) {
    step1Indicator.className =
      "w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center text-sm font-medium";
    step2Indicator.className =
      "w-8 h-8 rounded-full bg-gray-300 text-gray-600 flex items-center justify-center text-sm font-medium";
  } else {
    step1Indicator.className =
      "w-8 h-8 rounded-full bg-green-600 text-white flex items-center justify-center text-sm font-medium";
    step2Indicator.className =
      "w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center text-sm font-medium";
  }
}

function showStep(stepNumber) {
  // Hide all steps
  document.querySelectorAll(".step-content").forEach((step) => {
    step.classList.add("hidden");
  });

  // Show current step
  const currentStepElement = document.getElementById(`step${stepNumber}`);
  if (currentStepElement) {
    currentStepElement.classList.remove("hidden");
  }
}

// Image upload functions
function handleImageUpload(event) {
  const files = event.target.files;
  if (files.length > 0) {
    processFiles(Array.from(files));
  }
}

function setupDragAndDrop() {
  const uploadArea = document.querySelector(".border-dashed");
  if (!uploadArea) return;

  uploadArea.addEventListener("dragover", (e) => {
    e.preventDefault();
    uploadArea.classList.add("border-blue-400", "bg-blue-50");
  });

  uploadArea.addEventListener("dragleave", (e) => {
    e.preventDefault();
    uploadArea.classList.remove("border-blue-400", "bg-blue-50");
  });

  uploadArea.addEventListener("drop", (e) => {
    e.preventDefault();
    uploadArea.classList.remove("border-blue-400", "bg-blue-50");

    const files = Array.from(e.dataTransfer.files).filter((file) =>
      file.type.startsWith("image/")
    );

    if (files.length > 0) {
      processFiles(files);
    }
  });
}

function processFiles(files) {
  let processedCount = 0;
  const totalFiles = files.length;
  const isEditMode = document.getElementById("isEditMode").value === "true";
  files.forEach((file) => {
    // Skip empty files
    if (file.size === 0) {
      processedCount++;
      if (processedCount === totalFiles) {
        renderImageGrid();
      }
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      // 10MB limit
      showNotification(`File ${file.name} quá lớn (tối đa 10MB)`, "error");
      processedCount++;
      if (processedCount === totalFiles) {
        renderImageGrid(); // Render once after all files processed
      }
      return;
    }

    // Check for duplicate files (same name and size)
    const targetArray = isEditMode ? currentEditPhotos : uploadedImages;
    const isDuplicate = targetArray.some(
      (existing) => existing.name === file.name && existing.size === file.size
    );

    if (isDuplicate) {
      showNotification(`File ${file.name} đã tồn tại`, "warning");
      processedCount++;
      if (processedCount === totalFiles) {
        renderImageGrid();
      }
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      const imageData = {
        id: `img_${Date.now()}_${Math.floor(Math.random() * 1000)}`, // Simple string ID
        file: file,
        preview: e.target.result,
        name: file.name,
        size: file.size,
        isExisting: false, // Flag to identify new uploaded files
      };

      // Add to appropriate array based on mode
      if (isEditMode) {
        currentEditPhotos.push(imageData);
      } else {
        uploadedImages.push(imageData);
      }

      processedCount++;

      // Only render once after all files are processed
      if (processedCount === totalFiles) {
        renderImageGrid();
      }
    };
    reader.readAsDataURL(file);
  });
}

function renderImageGrid() {
  const imageGrid = document.getElementById("imageGrid");
  const noPhotosMessage = document.getElementById("noPhotosMessage");
  const coverPhotoInfo = document.getElementById("coverPhotoInfo");

  if (!imageGrid) return;

  imageGrid.innerHTML = "";

  // Hide cover photo section initially
  if (coverPhotoInfo) coverPhotoInfo.classList.add("hidden");

  // Determine which images to render
  const isEditMode = document.getElementById("isEditMode").value === "true";
  const imagesToRender = isEditMode ? currentEditPhotos : uploadedImages;

  // Show no photos message if no images
  if (imagesToRender.length === 0) {
    if (noPhotosMessage) noPhotosMessage.classList.remove("hidden");
    return;
  }

  if (noPhotosMessage) noPhotosMessage.classList.add("hidden");
  if (coverPhotoInfo) coverPhotoInfo.classList.remove("hidden");

  imagesToRender.forEach((imageData, index) => {
    // Create image element to get natural dimensions
    const img = new Image();
    img.onload = function () {
      const aspectRatio = this.naturalWidth / this.naturalHeight;

      const imageCard = document.createElement("div");
      const isSelected = selectedCoverPhotoId === imageData.id;
      imageCard.className = `relative group bg-white rounded-lg overflow-hidden shadow-sm border-2 break-inside-avoid mb-4 cursor-pointer transition-all ${
        isSelected
          ? "border-blue-500 ring-2 ring-blue-200"
          : "border-gray-200 hover:border-gray-300"
      }`;

      // Calculate natural height based on aspect ratio (fixed width)
      const cardWidth = 200; // Fixed width for consistency
      const naturalHeight = cardWidth / aspectRatio;

      // Determine image source and alt text based on mode
      const imageSrc = isEditMode
        ? imageData.url || imageData.preview
        : imageData.preview;
      const imageAlt = isEditMode
        ? imageData.title || imageData.name
        : imageData.name;
      const deleteFunction = isEditMode
        ? imageData.isExisting
          ? `deleteExistingPhoto('${imageData.id}')`
          : `removeImage('${imageData.id}')`
        : `removeImage('${imageData.id}')`;

      imageCard.innerHTML = `
        <div class="relative bg-gray-50" style="width: ${cardWidth}px; height: ${naturalHeight}px;" onclick="selectCoverPhoto('${
        imageData.id
      }')">
          <img
            src="${imageSrc}"
            alt="${imageAlt}"
            class="w-full h-full object-cover cursor-pointer"
            onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"
          />
          
          <!-- Fallback khi ảnh lỗi -->
          <div class="absolute inset-0 bg-gray-100 flex items-center justify-center hidden">
            <i class="fas fa-image text-gray-400 text-2xl"></i>
          </div>
          
          <!-- Cover selection indicator -->
          ${
            isSelected
              ? `
            <div class="absolute top-2 left-2 bg-blue-500 text-white rounded-full w-6 h-6 flex items-center justify-center">
              <i class="fas fa-check text-xs"></i>
            </div>
          `
              : ""
          }
          
          <!-- Nút xoá -->
          <button
            onclick="event.stopPropagation(); ${deleteFunction}"
            class="cursor-pointer absolute top-2 right-2 bg-red-500 hover:bg-red-600 text-white rounded-full size-6 flex items-center justify-center transition-all duration-200"
          >
            <i class="fas fa-close text-xs"></i>
          </button>
        </div>
        
        <!-- Thông tin file -->
        ${
          isSelected
            ? `
        <div class="p-2 bg-white">
            <p class="text-xs text-blue-600 font-medium">Ảnh bìa</p>
          </div>
        `
            : ""
        }
      `;

      imageGrid.appendChild(imageCard);
    };

    img.src = isEditMode
      ? imageData.url || imageData.preview
      : imageData.preview;
  });
}

function selectCoverPhoto(imageId) {
  selectedCoverPhotoId = imageId;

  // Find index for images
  const isEditMode = document.getElementById("isEditMode").value === "true";
  const imagesToSearch = isEditMode ? currentEditPhotos : uploadedImages;
  const index = imagesToSearch.findIndex((img) => img.id === imageId);
  selectedCoverPhotoIndex = index >= 0 ? index : null;

  renderImageGrid(); // Re-render to update selection UI
}

function removeImage(imageId) {
  const isEditMode = document.getElementById("isEditMode").value === "true";

  if (isEditMode) {
    // In edit mode, remove from currentEditPhotos
    currentEditPhotos = currentEditPhotos.filter((img) => img.id !== imageId);
  } else {
    // In create mode, remove from uploadedImages
    uploadedImages = uploadedImages.filter((img) => img.id !== imageId);
  }

  // If removed image was selected as cover, clear selection
  if (selectedCoverPhotoId === imageId) {
    selectedCoverPhotoId = null;
    selectedCoverPhotoIndex = null;
  }

  renderImageGrid();
}

function formatFileSize(bytes) {
  if (bytes === 0) return "0 Bytes";
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}

// Modal management functions
function openMyAlbumModal(myAlbumId = null) {
  currentMyAlbumId = myAlbumId;
  currentStep = 1;
  uploadedImages = [];

  const modal = document.getElementById("myAlbumModal");
  const title = document.getElementById("myAlbumModalTitle");
  const form = document.getElementById("myAlbumForm");

  // Check if elements exist
  if (!modal || !title || !form) {
    console.error("Required modal elements not found");
    return;
  }

  // Reset form and step
  form.reset();
  const hiddenIdField = document.getElementById("myAlbumId");
  if (hiddenIdField) {
    hiddenIdField.value = "";
  }

  // Reset edit mode
  const isEditModeField = document.getElementById("isEditMode");
  if (isEditModeField) {
    isEditModeField.value = "false";
  }

  // Reset cover photo selection
  selectedCoverPhotoId = null;
  selectedCoverPhotoIndex = null;

  // Reset image grid
  const imageGrid = document.getElementById("imageGrid");
  if (imageGrid) {
    imageGrid.innerHTML = "";
  }

  // Update step indicators
  updateStepIndicators();
  showStep(1);

  if (myAlbumId) {
    title.textContent = "Chỉnh sửa Album";
    loadMyAlbumData(myAlbumId);
  } else {
    title.textContent = "Tạo Album mới";
  }

  modal.classList.remove("hidden");
}

function closeMyAlbumModal() {
  const modal = document.getElementById("myAlbumModal");
  const form = document.getElementById("myAlbumForm");

  if (!modal || !form) {
    console.error("Modal or form elements not found");
    return;
  }
  document.querySelector('button[type="submit"]').textContent = "Tạo Album";

  modal.classList.add("hidden");
  form.reset();

  // Reset step and images
  currentStep = 1;
  uploadedImages = [];
  selectedCoverPhotoId = null;
  selectedCoverPhotoIndex = null;

  // Reset edit mode
  const isEditModeField = document.getElementById("isEditMode");
  if (isEditModeField) {
    isEditModeField.value = "false";
  }

  const title = document.getElementById("myAlbumModalTitle");
  if (title) {
    title.textContent = "Tạo Album mới";
  }

  currentEditAlbumId = null;
  currentEditPhotos = [];

  // Reset image grid
  const imageGrid = document.getElementById("imageGrid");
  if (imageGrid) {
    imageGrid.innerHTML = "";
  }

  // Clear any validation errors
  const errorMessages = modal.querySelectorAll(".error-message");
  errorMessages.forEach((msg) => msg.remove());
}

function loadMyAlbumData(myAlbumId) {
  fetch(`/admin/my-albums/${myAlbumId}`)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      // Populate form fields safely
      const idField = document.getElementById("myAlbumId");
      const nameField = document.getElementById("myAlbumName");
      const descriptionField = document.getElementById("myAlbumDescription");
      const isPublicField = document.getElementById("myAlbumIsPublic");

      if (idField) idField.value = data.id || "";
      if (nameField) nameField.value = data.name || "";
      if (descriptionField) descriptionField.value = data.description || "";
      if (isPublicField) isPublicField.checked = data.isPublic || false;

      openMyAlbumModal(myAlbumId);
    })
    .catch((error) => {
      console.error("Error loading my album data:", error);
      showNotification("Lỗi khi tải dữ liệu album: " + error.message, "error");
    });
}

function closeConfirmModal() {
  const confirmModal = document.getElementById("confirmModal");
  confirmModal.classList.add("hidden");
  window.myAlbumToDelete = null;
}

function confirmDelete() {
  const myAlbumId = window.myAlbumToDelete;
  if (!myAlbumId) {
    closeConfirmModal();
    return;
  }

  fetch(`/admin/my-albums/${myAlbumId}`, {
    method: "DELETE",
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.text(); // Delete endpoint returns text
    })
    .then((message) => {
      showNotification("Xóa album thành công!", "success");
      closeConfirmModal();
      // Reload page to reflect changes
      window.location.reload();
    })
    .catch((error) => {
      console.error("Error deleting my album:", error);
      showNotification("Lỗi khi xóa album: " + error.message, "error");
      closeConfirmModal();
    });
}

// Edit Album Functions - Using main form
function openMyAlbumModalForEdit(albumId) {
  currentEditAlbumId = albumId;

  // Set edit mode
  document.getElementById("isEditMode").value = "true";
  document.getElementById("myAlbumModalTitle").textContent = "Chỉnh sửa Album";
  document.querySelector('button[type="submit"]').textContent = "Cập nhật";
  // Open modal
  const modal = document.getElementById("myAlbumModal");
  modal.classList.remove("hidden");

  // Load album data
  loadAlbumForEdit(albumId);
}

function loadAlbumForEdit(albumId) {
  fetch(`/admin/my-albums/${albumId}`)
    .then((response) => response.json())
    .then((data) => {
      // Fill form with album data
      document.getElementById("myAlbumId").value = data.id;
      document.getElementById("myAlbumName").value = data.name;
      document.getElementById("myAlbumDescription").value =
        data.description || "";
      document.getElementById("myAlbumIsPublic").checked = data.isPublic;

      // Load existing photos
      loadAlbumPhotos(albumId);
    })
    .catch((error) => {
      console.error("Error loading album:", error);
      showNotification("Lỗi khi tải thông tin album", "error");
    });
}

function loadAlbumPhotos(albumId) {
  fetch(`/admin/my-albums/${albumId}/photos`)
    .then((response) => response.json())
    .then((photos) => {
      // Convert photos from DB to same format as uploadedImages
      currentEditPhotos = photos.map((photo) => ({
        id: photo.id,
        url: photo.url,
        preview: photo.url, // Use URL as preview for existing photos
        name: photo.title || "Photo",
        title: photo.title,
        size: photo.size || 0,
        file: null, // No file object for existing photos
        isExisting: true, // Flag to identify existing photos
      }));
      renderImageGrid();
    })
    .catch((error) => {
      console.error("Error loading photos:", error);
      showNotification("Lỗi khi tải ảnh album", "error");
    });
}

function deleteExistingPhoto(photoId) {
  if (confirm("Bạn có chắc chắn muốn xóa ảnh này?")) {
    fetch(`/admin/my-albums/${currentEditAlbumId}/photos/${photoId}`, {
      method: "DELETE",
    })
      .then((response) => {
        if (response.ok) {
          // Remove photo from current list
          currentEditPhotos = currentEditPhotos.filter(
            (photo) => photo.id !== photoId
          );
          // If removed photo was selected as cover, clear selection
          if (selectedCoverPhotoId === photoId) {
            selectedCoverPhotoId = null;
            selectedCoverPhotoIndex = null;
          }
          renderImageGrid();
          showNotification("Xóa ảnh thành công", "success");
        } else {
          throw new Error("Failed to delete photo");
        }
      })
      .catch((error) => {
        console.error("Error deleting photo:", error);
        showNotification("Lỗi khi xóa ảnh", "error");
      });
  }
}

// Setup form submission for both create and edit
document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("myAlbumForm");
  if (form) {
    form.addEventListener("submit", function (e) {
      e.preventDefault();

      const isEditMode = document.getElementById("isEditMode").value === "true";

      if (isEditMode) {
        // Edit mode - update existing album
        updateAlbum();
      } else {
        // Create mode - create new album
        createAlbum();
      }
    });
  }
});

function createAlbum() {
  // Create FormData manually (don't use form to avoid empty file input)
  const formData = new FormData();

  // Add form fields manually
  const nameInput = document.getElementById("myAlbumName");
  const descriptionInput = document.getElementById("myAlbumDescription");
  const idInput = document.getElementById("myAlbumId");
  const isPublicCheckbox = document.getElementById("myAlbumIsPublic");

  if (nameInput) formData.set("name", nameInput.value);
  if (descriptionInput) formData.set("description", descriptionInput.value);
  if (idInput && idInput.value) formData.set("id", idInput.value);
  if (isPublicCheckbox) formData.set("isPublic", isPublicCheckbox.checked);

  // Add uploaded images to FormData (filter out empty files)
  uploadedImages.forEach((imageData, index) => {
    if (imageData.file && imageData.file.size > 0) {
      formData.append(`images`, imageData.file);
    }
  });

  // Add cover photo information (send index instead of ID)
  if (selectedCoverPhotoIndex !== null) {
    formData.set("coverPhotoIndex", selectedCoverPhotoIndex);
  }

  // Show loading state
  const submitBtn = document.querySelector('button[type="submit"]');
  const originalText = submitBtn.textContent;
  submitBtn.textContent = "Đang tạo album...";
  submitBtn.disabled = true;

  fetch("/admin/my-albums", {
    method: "POST",
    body: formData,
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      showNotification("Tạo album thành công!", "success");
      closeMyAlbumModal();
      // Reload page to reflect changes
      window.location.reload();
    })
    .catch((error) => {
      console.error("Error creating album:", error);
      showNotification("Lỗi khi tạo album: " + error.message, "error");
    })
    .finally(() => {
      // Reset button state
      submitBtn.textContent = originalText;
      submitBtn.disabled = false;
    });
}

function updateAlbum() {
  const formData = new FormData();
  formData.set("name", document.getElementById("myAlbumName").value);
  formData.set(
    "description",
    document.getElementById("myAlbumDescription").value
  );
  formData.set("isPublic", document.getElementById("myAlbumIsPublic").checked);

  // Handle cover photo selection
  if (selectedCoverPhotoId) {
    // Check if selected cover photo is existing (from DB)
    const selectedPhoto = currentEditPhotos.find(
      (photo) => photo.id === selectedCoverPhotoId
    );

    if (selectedPhoto && selectedPhoto.isExisting) {
      // Cover photo is existing from DB - send coverId
      formData.set("coverId", selectedCoverPhotoId);
    } else if (selectedPhoto && !selectedPhoto.isExisting) {
      // Cover photo is new upload - send coverIndex
      const newPhotosArray = currentEditPhotos.filter(
        (photo) => !photo.isExisting
      );
      const coverIndex = newPhotosArray.findIndex(
        (photo) => photo.id === selectedCoverPhotoId
      );
      if (coverIndex >= 0) {
        formData.set("coverIndex", coverIndex);
      }
    }
  }
  // Add new photos to FormData (only files that are not existing)
  currentEditPhotos.forEach((imageData, index) => {
    if (imageData.file && imageData.file.size > 0 && !imageData.isExisting) {
      formData.append(`images`, imageData.file);
    }
  });

  // Show loading state
  const submitBtn = document.querySelector(
    '#myAlbumForm button[type="submit"]'
  );
  const originalText = submitBtn.textContent;
  submitBtn.textContent = "Đang cập nhật...";
  submitBtn.disabled = true;

  fetch(`/admin/my-albums/${currentEditAlbumId}`, {
    method: "PUT",
    body: formData,
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      showNotification("Cập nhật album thành công!", "success");
      closeMyAlbumModal();
      // Reload page to reflect changes
      window.location.reload();
    })
    .catch((error) => {
      console.error("Error updating album:", error);
      showNotification("Lỗi khi cập nhật album: " + error.message, "error");
    })
    .finally(() => {
      submitBtn.textContent = originalText;
      submitBtn.disabled = false;
    });
}

// Setup modal click outside to close
function setupModalClickOutside() {
  // My Album modal
  const myAlbumModal = document.getElementById("myAlbumModal");
  if (myAlbumModal) {
    myAlbumModal.addEventListener("click", function (e) {
      if (e.target === myAlbumModal) {
        closeMyAlbumModal();
      }
    });
  }

  // Confirm modal
  const confirmModal = document.getElementById("confirmModal");
  if (confirmModal) {
    confirmModal.addEventListener("click", function (e) {
      if (e.target === confirmModal) {
        closeConfirmModal();
      }
    });
  }
}

// Search and Filter Functions
function setupSearchAndFilter() {
  // Set form values from URL parameters on page load
  setFormValuesFromURL();

  // Auto-submit form when select values change
  const sortSelect = document.querySelector('select[name="sort"]');
  const isPublicSelect = document.querySelector('select[name="isPublic"]');

  if (sortSelect) {
    sortSelect.addEventListener("change", function () {
      // Only send sort parameter
      const url = new URL(window.location);
      url.searchParams.set("sort", this.value);
      url.searchParams.delete("page"); // Reset to first page
      window.location.href = url.toString();
    });
  }

  if (isPublicSelect) {
    isPublicSelect.addEventListener("change", function () {
      // Only send isPublic parameter
      const url = new URL(window.location);
      if (this.value) {
        url.searchParams.set("isPublic", this.value);
      } else {
        url.searchParams.delete("isPublic");
      }
      url.searchParams.delete("page"); // Reset to first page
      window.location.href = url.toString();
    });
  }

  // Search input with debounce
  const searchInput = document.querySelector('input[name="search"]');
  if (searchInput) {
    let searchTimeout;
    searchInput.addEventListener("input", function () {
      clearTimeout(searchTimeout);
      searchTimeout = setTimeout(() => {
        // Only send search parameter
        const url = new URL(window.location);
        if (this.value.trim()) {
          url.searchParams.set("search", this.value.trim());
        } else {
          url.searchParams.delete("search");
        }
        url.searchParams.delete("page"); // Reset to first page
        window.location.href = url.toString();
      }, 500); // Wait 500ms after user stops typing
    });
  }
}

function setFormValuesFromURL() {
  const urlParams = new URLSearchParams(window.location.search);

  // Set search input
  const searchInput = document.querySelector('input[name="search"]');
  if (searchInput) {
    searchInput.value = urlParams.get("search") || "";
  }

  // Set isPublic select
  const isPublicSelect = document.querySelector('select[name="isPublic"]');
  if (isPublicSelect) {
    isPublicSelect.value = urlParams.get("isPublic") || "";
  }

  // Set sort select
  const sortSelect = document.querySelector('select[name="sort"]');
  if (sortSelect) {
    sortSelect.value = urlParams.get("sort") || "name";
  }
}
