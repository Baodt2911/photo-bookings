// Package Management JavaScript
let currentPackageId = null;

// Initialize page
document.addEventListener("DOMContentLoaded", function () {
  setupFormSubmission();
  setupModalClickOutside();
  setupSearchAndFilter();
});

// Image upload functions
function previewImage(input) {
  const file = input.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = function (e) {
      document.getElementById("previewImg").src = e.target.result;
      document.getElementById("imagePreview").classList.remove("hidden");
      document.getElementById("imageUpload").classList.add("hidden");
    };
    reader.readAsDataURL(file);
  }
}

function removeImage() {
  document.getElementById("packageImage").value = "";
  document.getElementById("imagePreview").classList.add("hidden");
  document.getElementById("imageUpload").classList.remove("hidden");
  document.getElementById("previewImg").src = "";
}

function openPackageModal(packageId = null) {
  currentPackageId = packageId;
  const modal = document.getElementById("packageModal");
  const title = document.getElementById("modalTitle");
  const form = document.getElementById("packageForm");
  // Reset form and hide image preview
  form.reset();
  removeImage();

  if (packageId) {
    title.textContent = "Chỉnh sửa Gói";
    loadPackageData(packageId);
  } else {
    title.textContent = "Thêm Gói Mới";
  }

  modal.classList.remove("hidden");
}

function closePackageModal() {
  document.getElementById("packageModal").classList.add("hidden");
  currentPackageId = null;
}

function loadPackageData(packageId) {
  fetch(`/admin/packages/${packageId}`)
    .then((response) => {
      if (response.ok) {
        return response.json();
      } else {
        throw new Error("Package not found");
      }
    })
    .then((data) => {
      document.getElementById("packageName").value = data.name || "";
      document.getElementById("packageSlug").value = data.slug || "";
      document.getElementById("packageDescription").value =
        data.description || "";
      document.getElementById("packageIncludes").value = data.includes || "";
      document.getElementById("packagePrice").value = data.price || "";
      document.getElementById("packageDuration").value =
        data.durationMinutes || "";
      document.getElementById("packageMaxPeople").value = data.maxPeople || "";

      // Show existing image if available
      if (data.imageUrl) {
        document.getElementById("previewImg").src = data.imageUrl;
        document.getElementById("imagePreview").classList.remove("hidden");
        document.getElementById("imageUpload").classList.add("hidden");
      }
    })
    .catch((error) => {
      console.error("Error loading package:", error);
      showNotification("Lỗi khi tải thông tin gói: " + error.message, "error");
    });
}

function editPackage(packageId) {
  openPackageModal(packageId);
}

function deletePackage(packageId) {
  // Create custom confirmation modal
  const confirmModal = document.createElement("div");
  confirmModal.className =
    "fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50";
  confirmModal.innerHTML = `
        <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4 shadow-xl">
            <div class="flex items-center mb-4">
                <div class="bg-red-100 rounded-full p-3 mr-4">
                    <i class="fas fa-exclamation-triangle text-red-600 text-xl"></i>
                </div>
                <div>
                    <h3 class="text-lg font-semibold text-gray-900">Xác nhận xóa</h3>
                    <p class="text-sm text-gray-600">Hành động này không thể hoàn tác</p>
                </div>
            </div>
            <p class="text-gray-700 mb-6">Bạn có chắc chắn muốn xóa gói này không?</p>
            <div class="flex justify-end space-x-3">
                <button onclick="closeConfirmModal()" class="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                    Hủy
                </button>
                <button onclick="confirmDelete('${packageId}')" class="px-4 py-2 bg-red-600 text-white hover:bg-red-700 rounded-lg transition-colors">
                    <i class="fas fa-trash mr-2"></i>Xóa
                </button>
            </div>
        </div>
    `;
  document.body.appendChild(confirmModal);
}

function closeConfirmModal() {
  const modal = document.querySelector(".fixed.inset-0.bg-black.bg-opacity-50");
  if (modal) {
    modal.remove();
  }
}

function confirmDelete(packageId) {
  closeConfirmModal();

  // Show loading state
  const deleteButton = document.querySelector(
    `[data-package-id="${packageId}"]`
  );
  const originalContent = deleteButton.innerHTML;
  deleteButton.innerHTML = '<i class="fas fa-spinner fa-spin text-sm"></i>';
  deleteButton.disabled = true;

  fetch(`/admin/packages/${packageId}`, {
    method: "DELETE",
  })
    .then((response) => {
      if (response.ok) {
        return response.text();
      } else {
        throw new Error("Delete failed");
      }
    })
    .then((data) => {
      // Reload page to refresh data from server
      window.location.reload();
    })
    .catch((error) => {
      console.error("Error deleting package:", error);
      showNotification("Lỗi khi xóa gói", "error");
      // Restore button state
      deleteButton.innerHTML = originalContent;
      deleteButton.disabled = false;
    });
}

function togglePackageStatus(packageId) {
  // Show loading state
  const toggleButton = document.querySelector(
    `[data-package-id="${packageId}"]`
  );
  const originalContent = toggleButton.innerHTML;
  toggleButton.innerHTML = '<i class="fas fa-spinner fa-spin text-sm"></i>';
  toggleButton.disabled = true;

  fetch(`/admin/packages/${packageId}/toggle-status`, {
    method: "POST",
  })
    .then((response) => response.json())
    .then((data) => {
      // Reload page to refresh data from server
      window.location.reload();
    })
    .catch((error) => {
      console.error("Error toggling status:", error);
      showNotification("Lỗi khi thay đổi trạng thái", "error");
      // Restore button state
      toggleButton.innerHTML = originalContent;
      toggleButton.disabled = false;
    });
}

// Handle form submission
function setupFormSubmission() {
  const packageForm = document.getElementById("packageForm");
  if (packageForm) {
    packageForm.addEventListener("submit", function (e) {
      e.preventDefault();

      // Show loading state
      const submitButton = this.querySelector('button[type="submit"]');
      const originalContent = submitButton.innerHTML;
      submitButton.innerHTML =
        '<i class="fas fa-spinner fa-spin mr-2"></i>Đang lưu...';
      submitButton.disabled = true;

      const formData = new FormData();

      // Add form fields (same for both create and update)
      formData.append("name", document.getElementById("packageName").value);
      formData.append("slug", document.getElementById("packageSlug").value);
      formData.append(
        "description",
        document.getElementById("packageDescription").value
      );
      formData.append(
        "includes",
        document.getElementById("packageIncludes").value
      );
      formData.append(
        "price",
        parseFloat(document.getElementById("packagePrice").value)
      );
      formData.append(
        "durationMinutes",
        parseInt(document.getElementById("packageDuration").value)
      );
      formData.append(
        "maxPeople",
        parseInt(document.getElementById("packageMaxPeople").value)
      );

      // Add image file if selected
      const imageFile = document.getElementById("packageImage").files[0];
      if (imageFile) {
        formData.append("image", imageFile);
      }

      const url = currentPackageId
        ? `/admin/packages/${currentPackageId}`
        : "/admin/packages";
      const method = currentPackageId ? "PUT" : "POST";

      fetch(url, {
        method: method,
        body: formData,
      })
        .then((response) => {
          if (response.ok) {
            return response.json();
          } else {
            return response.text().then((text) => {
              throw new Error(text || "Request failed");
            });
          }
        })
        .then((data) => {
          if (currentPackageId) {
            showNotification("Cập nhật gói thành công", "success");
          } else {
            showNotification("Tạo gói thành công", "success");
          }

          closePackageModal();
          // Reload page to refresh data from server
          setTimeout(() => {
            window.location.reload();
          }, 1000);
        })
        .catch((error) => {
          console.error("Error saving package:", error);
          showNotification("Lỗi khi lưu gói: " + error.message, "error");
          // Restore button state
          submitButton.innerHTML = originalContent;
          submitButton.disabled = false;
        });
    });
  }
}

// Notification system
// Notification functions now use global notification system
// showNotification and closeNotification are available globally from notification.js

// Close modal when clicking outside
function setupModalClickOutside() {
  const packageModal = document.getElementById("packageModal");
  if (packageModal) {
    packageModal.addEventListener("click", function (e) {
      if (e.target === this) {
        closePackageModal();
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
  const statusSelect = document.querySelector('select[name="status"]');

  if (sortSelect) {
    sortSelect.addEventListener("change", function () {
      // Only send sort parameter
      const url = new URL(window.location);
      url.searchParams.set("sort", this.value);
      url.searchParams.delete("page"); // Reset to first page
      window.location.href = url.toString();
    });
  }

  if (statusSelect) {
    statusSelect.addEventListener("change", function () {
      // Only send status parameter
      const url = new URL(window.location);
      if (this.value) {
        url.searchParams.set("status", this.value);
      } else {
        url.searchParams.delete("status");
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

  // Set status select
  const statusSelect = document.querySelector('select[name="status"]');
  if (statusSelect) {
    statusSelect.value = urlParams.get("status") || "";
  }

  // Set sort select
  const sortSelect = document.querySelector('select[name="sort"]');
  if (sortSelect) {
    sortSelect.value = urlParams.get("sort") || "name";
  }
}
