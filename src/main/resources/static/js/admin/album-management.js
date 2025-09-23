// Album Management JavaScript

let currentAlbumId = null;

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  setupSearchAndFilter();
  setupAlbumMenu();
  setupToggleSwitches();
  setupFormSubmission();
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
    // Position dropdown near the button
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
function openAlbumModal() {
  const modal = document.getElementById("albumModal");
  const title = document.getElementById("albumModalTitle");
  const form = document.getElementById("albumForm");

  if (modal && title && form) {
    title.textContent = "Tạo album";
    form.reset();
    document.getElementById("albumId").value = "";

    // Reset toggle switches visually
    resetToggleSwitches();

    modal.classList.remove("hidden");
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
  // TODO: Implement edit album functionality
  console.log("Edit album:", albumId);
  openAlbumModal();
}

function deleteAlbum(albumId) {
  const modal = document.getElementById("confirmModal");
  const message = document.getElementById("confirmMessage");
  const deleteBtn = document.getElementById("confirmDeleteBtn");

  if (modal && message && deleteBtn) {
    message.textContent =
      "Bạn có chắc chắn muốn xóa album này? Hành động này không thể hoàn tác.";
    deleteBtn.onclick = function () {
      confirmDelete(albumId);
    };
    modal.classList.remove("hidden");
  }
}

function closeConfirmModal() {
  const modal = document.getElementById("confirmModal");
  if (modal) {
    modal.classList.add("hidden");
  }
}

function confirmDelete(albumId) {
  // Send delete request to server endpoint /admin/albums/{id}
  fetch(`/admin/albums/${albumId}`, {
    method: "DELETE",
  })
    .then((response) => response.text())
    .then((data) => {
      console.log("Album deleted successfully:", data);
      closeConfirmModal();
      // Refresh page or remove album from grid
      window.location.reload();
    })
    .catch((error) => {
      console.error("Error:", error);
      alert("Lỗi khi xóa album: " + error.message);
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

      if (albumId) {
        updateAlbum(formData, albumId);
      } else {
        createAlbum(formData);
      }
    });
  }
}

function createAlbum(formData) {
  // TODO: Implement create album functionality
  console.log("Create album:", formData);

  // Send to server endpoint /admin/albums
  fetch("/admin/albums", {
    method: "POST",
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.id) {
        console.log("Album created successfully:", data);
        closeAlbumModal();
        // Refresh page or add album to grid
        window.location.reload();
      } else {
        console.error("Error creating album:", data);
        alert("Lỗi khi tạo album: " + (data.message || "Unknown error"));
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      alert("Lỗi khi tạo album: " + error.message);
    });
}

function updateAlbum(formData, albumId) {
  // TODO: Implement update album functionality
  console.log("Update album:", albumId, formData);

  // Send to server endpoint /admin/albums/{id}
  fetch(`/admin/albums/${albumId}`, {
    method: "PUT",
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.id) {
        console.log("Album updated successfully:", data);
        closeAlbumModal();
        // Refresh page or update album in grid
        window.location.reload();
      } else {
        console.error("Error updating album:", data);
        alert("Lỗi khi cập nhật album: " + (data.message || "Unknown error"));
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      alert("Lỗi khi cập nhật album: " + error.message);
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
