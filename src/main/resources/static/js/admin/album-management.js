// Album Management JavaScript

let currentAlbumId = null;

// Function to navigate to album view
function goToAlbum(element) {
  const albumId = element.dataset.albumId;
  if (albumId) {
    window.location.href = "/admin/albums/" + albumId + "/view";
  }
}

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  setupSearchAndFilter();
  setupAlbumMenu();
  setupToggleSwitches();
  setupFormSubmission();
  setupDriveLinkValidation();
});

// Search and Filter Functions
function setupSearchAndFilter() {
  // Search input with debounce
  const searchInput = document.querySelector('input[placeholder="Tìm kiếm"]');
  if (searchInput) {
    let searchTimeout;
    searchInput.addEventListener("input", function () {
      clearTimeout(searchTimeout);
      searchTimeout = setTimeout(() => {
        // TODO: Implement search functionality
        console.log("Search:", this.value);
      }, 500);
    });
  }
}

// Album Menu Functions
function setupAlbumMenu() {
  const editBtn = document.getElementById("editAlbumBtn");
  const deleteBtn = document.getElementById("deleteAlbumBtn");

  if (editBtn) {
    editBtn.addEventListener("click", function () {
      if (currentAlbumId) {
        editAlbum(currentAlbumId);
      }
      hideAlbumMenu();
    });
  }

  if (deleteBtn) {
    deleteBtn.addEventListener("click", function () {
      if (currentAlbumId) {
        deleteAlbum(currentAlbumId);
      }
      hideAlbumMenu();
    });
  }

  // Hide menu when clicking outside
  document.addEventListener("click", function (event) {
    const dropdown = document.getElementById("albumMenuDropdown");
    if (dropdown && !dropdown.contains(event.target)) {
      hideAlbumMenu();
    }
  });
}

function showAlbumMenu(event, albumId) {
  event.preventDefault();
  event.stopPropagation();

  currentAlbumId = albumId;

  const dropdown = document.getElementById("albumMenuDropdown");
  if (dropdown) {
    // Position dropdown below the button
    const rect = event.target.getBoundingClientRect();
    dropdown.style.left = rect.left + "px";
    dropdown.style.top = rect.bottom + 5 + "px";
    dropdown.classList.remove("hidden");
  }
}

function hideAlbumMenu() {
  const dropdown = document.getElementById("albumMenuDropdown");
  if (dropdown) {
    dropdown.classList.add("hidden");
  }
  currentAlbumId = null;
}

// Album Modal Functions
function openAlbumModal(albumId = null) {
  const modal = document.getElementById("albumModal");
  const title = document.getElementById("albumModalTitle");
  const form = document.getElementById("albumForm");
  const submitBtn = document.getElementById("submitBtn");

  if (modal && title && form && submitBtn) {
    if (albumId) {
      title.textContent = "Chỉnh sửa album";
      submitBtn.textContent = "Cập nhật";
      loadAlbumDataForEdit(albumId);
    } else {
      title.textContent = "Tạo album";
      submitBtn.textContent = "Tạo ngay";
      form.reset();
      document.getElementById("albumId").value = "";
      resetToggleSwitches();
    }

    modal.classList.remove("hidden");
  }
}

function loadAlbumDataForEdit(albumId) {
  // Show loading state
  const form = document.getElementById("albumForm");
  if (form) {
    form.style.opacity = "0.5";
    form.style.pointerEvents = "none";
  }

  // Fetch album data
  fetch(`/admin/albums/${albumId}/edit`)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((album) => {
      console.log("Loaded album data:", album);

      // Populate form fields
      document.getElementById("albumId").value = album.id;
      document.getElementById("albumName").value = album.name || "";
      document.getElementById("albumCustomerName").value =
        album.customerName || "";
      document.getElementById("albumDriveFolderLink").value =
        album.driveFolderLink || "";
      // Không hiển thị password cũ để bảo mật
      document.getElementById("albumPassword").value = "";
      document.getElementById("albumLimitSelection").value =
        album.limitSelection || "";

      // Set toggle switches
      setToggleSwitch("albumAllowDownload", album.allowDownload);
      setToggleSwitch("albumAllowComment", album.allowComment);
      // Check password protect toggle based on isPassword
      setToggleSwitch("albumPasswordProtect", album.isPassword);
      setToggleSwitch(
        "albumLimitSelectionEnabled",
        album.limitSelection != null && album.limitSelection > 0
      );

      // Handle conditional inputs
      handleConditionalInputs(document.getElementById("albumPasswordProtect"));
      handleConditionalInputs(
        document.getElementById("albumLimitSelectionEnabled")
      );

      // Reset form opacity
      if (form) {
        form.style.opacity = "1";
        form.style.pointerEvents = "auto";
      }
    })
    .catch((error) => {
      console.error("Error loading album data:", error);
      showNotification(
        "Lỗi khi tải thông tin album: " + error.message,
        "error"
      );

      // Reset form opacity
      if (form) {
        form.style.opacity = "1";
        form.style.pointerEvents = "auto";
      }
    });
}

function setToggleSwitch(toggleId, isChecked) {
  const toggle = document.getElementById(toggleId);
  const label = document.querySelector(`label[for="${toggleId}"]`);
  const span = label.querySelector("span");

  if (toggle && label && span) {
    toggle.checked = isChecked;

    if (isChecked) {
      label.classList.remove("bg-gray-300");
      label.classList.add("bg-green-500");
      span.classList.remove("translate-x-0.5");
      span.classList.add("translate-x-6");
    } else {
      label.classList.remove("bg-green-500");
      label.classList.add("bg-gray-300");
      span.classList.remove("translate-x-6");
      span.classList.add("translate-x-0.5");
    }
  }
}

function resetToggleSwitches() {
  // Reset Allow Comment (default: unchecked)
  const allowCommentToggle = document.getElementById("albumAllowComment");
  const allowCommentLabel = document.querySelector(
    'label[for="albumAllowComment"]'
  );
  const allowCommentSpan = allowCommentLabel.querySelector("span");
  allowCommentToggle.checked = false;
  allowCommentLabel.classList.remove("bg-green-500");
  allowCommentLabel.classList.add("bg-gray-300");
  allowCommentSpan.classList.remove("translate-x-6");
  allowCommentSpan.classList.add("translate-x-0.5");

  // Reset Password Protect (default: unchecked)
  const passwordToggle = document.getElementById("albumPasswordProtect");
  const passwordLabel = document.querySelector(
    'label[for="albumPasswordProtect"]'
  );
  const passwordSpan = passwordLabel.querySelector("span");
  passwordToggle.checked = false;
  passwordLabel.classList.remove("bg-green-500");
  passwordLabel.classList.add("bg-gray-300");
  passwordSpan.classList.remove("translate-x-6");
  passwordSpan.classList.add("translate-x-0.5");

  // Hide password input
  const passwordInput = document.getElementById("albumPassword");
  passwordInput.classList.add("hidden");
  passwordInput.required = false;
  passwordInput.value = "";

  // Reset Allow Download (default: unchecked)
  const allowDownloadToggle = document.getElementById("albumAllowDownload");
  const allowDownloadLabel = document.querySelector(
    'label[for="albumAllowDownload"]'
  );
  const allowDownloadSpan = allowDownloadLabel.querySelector("span");
  allowDownloadToggle.checked = false;
  allowDownloadLabel.classList.remove("bg-green-500");
  allowDownloadLabel.classList.add("bg-gray-300");
  allowDownloadSpan.classList.remove("translate-x-6");
  allowDownloadSpan.classList.add("translate-x-0.5");

  // Reset Limit Selection (default: unchecked)
  const limitToggle = document.getElementById("albumLimitSelectionEnabled");
  const limitLabel = document.querySelector(
    'label[for="albumLimitSelectionEnabled"]'
  );
  const limitSpan = limitLabel.querySelector("span");
  limitToggle.checked = false;
  limitLabel.classList.remove("bg-green-500");
  limitLabel.classList.add("bg-gray-300");
  limitSpan.classList.remove("translate-x-6");
  limitSpan.classList.add("translate-x-0.5");

  // Hide limit input
  const limitInput = document.getElementById("albumLimitSelection");
  limitInput.classList.add("hidden");
  limitInput.required = false;
  limitInput.value = "";
}

function closeAlbumModal() {
  const modal = document.getElementById("albumModal");
  if (modal) {
    modal.classList.add("hidden");
  }
}

function editAlbum(albumId) {
  console.log("Edit album:", albumId);
  openAlbumModal(albumId);
}

function shareAlbum(albumId) {
  console.log("Share album:", albumId);
  // TODO: Implement share functionality
  showNotification("Tính năng chia sẻ đang được phát triển", "info");
}

function deleteAlbum(albumId) {
  // Store albumId for confirmation
  window.albumToDelete = albumId;

  // Show confirmation modal
  const confirmModal = document.getElementById("confirmModal");
  const confirmMessage = document.getElementById("confirmMessage");
  confirmMessage.textContent =
    "Bạn có chắc chắn muốn xóa album này? Hành động này không thể hoàn tác.";

  confirmModal.classList.remove("hidden");
}

function closeConfirmModal() {
  const modal = document.getElementById("confirmModal");
  if (modal) {
    modal.classList.add("hidden");
  }
  window.albumToDelete = null;
}

function confirmDelete() {
  const albumId = window.albumToDelete;
  if (!albumId) {
    closeConfirmModal();
    return;
  }

  fetch(`/admin/albums/${albumId}`, {
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
      console.error("Error deleting album:", error);
      showNotification("Lỗi khi xóa album: " + error.message, "error");
      closeConfirmModal();
    });
}

// Form submission
function setupFormSubmission() {
  const form = document.getElementById("albumForm");
  if (form) {
    form.addEventListener("submit", function (e) {
      e.preventDefault();

      const formData = new FormData(form);
      const albumId = document.getElementById("albumId").value;

      // Fix checkbox values for FormData
      fixCheckboxValues(formData);

      if (albumId) {
        updateAlbum(formData, albumId);
      } else {
        createAlbum(formData);
      }
    });
  }
}

function fixCheckboxValues(formData) {
  // Fix allowComment checkbox
  const allowCommentCheckbox = document.getElementById("albumAllowComment");
  if (allowCommentCheckbox.checked) {
    formData.set("allowComment", "true");
  } else {
    formData.set("allowComment", "false");
  }

  // Fix allowDownload checkbox
  const allowDownloadCheckbox = document.getElementById("albumAllowDownload");
  if (allowDownloadCheckbox.checked) {
    formData.set("allowDownload", "true");
  } else {
    formData.set("allowDownload", "false");
  }

  // Fix passwordProtect checkbox
  const passwordProtectCheckbox = document.getElementById(
    "albumPasswordProtect"
  );
  if (passwordProtectCheckbox.checked) {
    formData.set("passwordProtect", "true");
  } else {
    formData.set("passwordProtect", "false");
  }

  // Fix limitSelectionEnabled checkbox
  const limitSelectionCheckbox = document.getElementById(
    "albumLimitSelectionEnabled"
  );
  if (limitSelectionCheckbox.checked) {
    formData.set("limitSelectionEnabled", "true");
  } else {
    formData.set("limitSelectionEnabled", "false");
  }
}

function createAlbum(formData) {
  console.log("Create album:", formData);

  // Show loading state
  const submitBtn = document.getElementById("submitBtn");
  const originalText = submitBtn.textContent;
  submitBtn.textContent = "Đang tạo...";
  submitBtn.disabled = true;

  // Send to server endpoint /admin/albums
  fetch("/admin/albums", {
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
      if (data.id) {
        console.log("Album created successfully:", data);
        showNotification("Tạo album thành công!", "success");
        closeAlbumModal();
        // Refresh page to reflect changes
        window.location.reload();
      } else {
        console.error("Error creating album:", data);
        showNotification(
          "Lỗi khi tạo album: " + (data.message || "Unknown error"),
          "error"
        );
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      showNotification("Lỗi khi tạo album: " + error.message, "error");
    })
    .finally(() => {
      // Reset button state
      submitBtn.textContent = originalText;
      submitBtn.disabled = false;
    });
}

function updateAlbum(formData, albumId) {
  console.log("Update album:", albumId, formData);

  // Show loading state
  const submitBtn = document.getElementById("submitBtn");
  const originalText = submitBtn.textContent;
  submitBtn.textContent = "Đang cập nhật...";
  submitBtn.disabled = true;

  // Send to server endpoint /admin/albums/{id}
  fetch(`/admin/albums/${albumId}`, {
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
      if (data.id) {
        console.log("Album updated successfully:", data);
        showNotification("Cập nhật album thành công!", "success");
        closeAlbumModal();
        // Refresh page to reflect changes
        window.location.reload();
      } else {
        console.error("Error updating album:", data);
        showNotification(
          "Lỗi khi cập nhật album: " + (data.message || "Unknown error"),
          "error"
        );
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      showNotification("Lỗi khi cập nhật album: " + error.message, "error");
    })
    .finally(() => {
      // Reset button state
      submitBtn.textContent = originalText;
      submitBtn.disabled = false;
    });
}

// Toggle Switch Functions
function setupToggleSwitches() {
  const toggles = document.querySelectorAll(
    'input[type="checkbox"][class="sr-only"]'
  );

  toggles.forEach((toggle) => {
    toggle.addEventListener("change", function () {
      const label = document.querySelector(`label[for="${this.id}"]`);
      const span = label.querySelector("span");

      if (this.checked) {
        label.classList.remove("bg-gray-300");
        label.classList.add("bg-green-500");
        span.classList.remove("translate-x-0.5");
        span.classList.add("translate-x-6");
      } else {
        label.classList.remove("bg-green-500");
        label.classList.add("bg-gray-300");
        span.classList.remove("translate-x-6");
        span.classList.add("translate-x-0.5");
      }

      // Handle conditional input visibility
      handleConditionalInputs(this);
    });
  });
}

function handleConditionalInputs(toggle) {
  // Password input visibility
  if (toggle.id === "albumPasswordProtect") {
    const passwordInput = document.getElementById("albumPassword");
    if (toggle.checked) {
      passwordInput.classList.remove("hidden");
      passwordInput.required = true;
    } else {
      passwordInput.classList.add("hidden");
      passwordInput.required = false;
      passwordInput.value = ""; // Clear password when disabled
    }
  }

  // Limit selection input visibility
  if (toggle.id === "albumLimitSelectionEnabled") {
    const limitInput = document.getElementById("albumLimitSelection");
    if (toggle.checked) {
      limitInput.classList.remove("hidden");
      limitInput.required = true;
    } else {
      limitInput.classList.add("hidden");
      limitInput.required = false;
      limitInput.value = ""; // Clear limit when disabled
    }
  }
}

// Google Drive Link Validation Functions
function setupDriveLinkValidation() {
  const driveInput = document.getElementById("albumDriveFolderLink");

  if (driveInput) {
    // Auto-validate when user stops typing
    let validationTimeout;
    driveInput.addEventListener("input", function () {
      clearTimeout(validationTimeout);
      validationTimeout = setTimeout(() => {
        if (this.value.trim()) {
          validateDriveLink(this.value.trim());
        } else {
          resetDriveValidation();
        }
      }, 1000);
    });
  }
}

function validateDriveLink(driveLink) {
  const driveInput = document.getElementById("albumDriveFolderLink");
  const loadingMsg = document.getElementById("driveLoadingMessage");
  const errorMsg = document.getElementById("driveErrorMessage");
  const successMsg = document.getElementById("driveSuccessMessage");
  const errorIcon = document.getElementById("driveErrorIcon");
  const successIcon = document.getElementById("driveSuccessIcon");
  const loadingIcon = document.getElementById("driveLoadingIcon");
  const statusDiv = document.getElementById("driveLinkStatus");

  // Show loading state
  showDriveLoading();

  // Make API call to validate drive link
  fetch("/admin/albums/validate-drive-link", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: `driveLink=${encodeURIComponent(driveLink)}`,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.valid) {
        showDriveSuccess();
      } else {
        showDriveError(data.message);
      }
    })
    .catch((error) => {
      console.error("Error validating drive link:", error);
      showDriveError("Lỗi khi kiểm tra link. Vui lòng thử lại.");
    })
    .finally(() => {
      hideDriveLoading();
    });
}

function showDriveLoading() {
  const loadingMsg = document.getElementById("driveLoadingMessage");
  const errorMsg = document.getElementById("driveErrorMessage");
  const successMsg = document.getElementById("driveSuccessMessage");
  const errorIcon = document.getElementById("driveErrorIcon");
  const successIcon = document.getElementById("driveSuccessIcon");
  const loadingIcon = document.getElementById("driveLoadingIcon");
  const driveInput = document.getElementById("albumDriveFolderLink");

  // Hide all other messages and icons first
  errorMsg.classList.add("hidden");
  successMsg.classList.add("hidden");
  errorIcon.classList.add("hidden");
  successIcon.classList.add("hidden");

  // Show loading message and icon
  loadingMsg.classList.remove("hidden");
  loadingIcon.classList.remove("hidden");

  // Reset input border
  driveInput.classList.remove("border-red-500", "border-green-500");
  driveInput.classList.add("border-gray-300");
}

function hideDriveLoading() {
  const loadingMsg = document.getElementById("driveLoadingMessage");
  const loadingIcon = document.getElementById("driveLoadingIcon");
  loadingMsg.classList.add("hidden");
  loadingIcon.classList.add("hidden");
}

function showDriveError(message) {
  const errorMsg = document.getElementById("driveErrorMessage");
  const successMsg = document.getElementById("driveSuccessMessage");
  const loadingMsg = document.getElementById("driveLoadingMessage");
  const errorIcon = document.getElementById("driveErrorIcon");
  const successIcon = document.getElementById("driveSuccessIcon");
  const loadingIcon = document.getElementById("driveLoadingIcon");
  const driveInput = document.getElementById("albumDriveFolderLink");

  // Hide all other messages and icons first
  successMsg.classList.add("hidden");
  loadingMsg.classList.add("hidden");
  successIcon.classList.add("hidden");
  loadingIcon.classList.add("hidden");

  // Show error message with server response
  errorMsg.classList.remove("hidden");
  errorMsg.querySelector("p").textContent = message;

  // Update input styling
  driveInput.classList.remove("border-gray-300", "border-green-500");
  driveInput.classList.add("border-red-500");

  // Show only error icon
  errorIcon.classList.remove("hidden");
}

function showDriveSuccess() {
  const errorMsg = document.getElementById("driveErrorMessage");
  const successMsg = document.getElementById("driveSuccessMessage");
  const loadingMsg = document.getElementById("driveLoadingMessage");
  const errorIcon = document.getElementById("driveErrorIcon");
  const successIcon = document.getElementById("driveSuccessIcon");
  const loadingIcon = document.getElementById("driveLoadingIcon");
  const driveInput = document.getElementById("albumDriveFolderLink");

  // Hide all other messages and icons first
  errorMsg.classList.add("hidden");
  successMsg.classList.add("hidden");
  loadingMsg.classList.add("hidden");
  errorIcon.classList.add("hidden");
  loadingIcon.classList.add("hidden");

  // Update input styling
  driveInput.classList.remove("border-gray-300", "border-red-500");
  driveInput.classList.add("border-green-500");

  // Show only success icon
  successIcon.classList.remove("hidden");
}

function resetDriveValidation() {
  const errorMsg = document.getElementById("driveErrorMessage");
  const successMsg = document.getElementById("driveSuccessMessage");
  const loadingMsg = document.getElementById("driveLoadingMessage");
  const errorIcon = document.getElementById("driveErrorIcon");
  const successIcon = document.getElementById("driveSuccessIcon");
  const loadingIcon = document.getElementById("driveLoadingIcon");
  const driveInput = document.getElementById("albumDriveFolderLink");

  // Hide all messages and icons
  errorMsg.classList.add("hidden");
  successMsg.classList.add("hidden");
  loadingMsg.classList.add("hidden");
  errorIcon.classList.add("hidden");
  successIcon.classList.add("hidden");
  loadingIcon.classList.add("hidden");

  // Reset input styling
  driveInput.classList.remove("border-red-500", "border-green-500");
  driveInput.classList.add("border-gray-300");
}
