// Global Notification System for Admin Panel
class NotificationManager {
  constructor() {
    this.notifications = [];
    this.maxNotifications = 5;
  }

  show(message, type = "info", duration = 5000) {
    const notification = this.createNotification(message, type);
    this.addToDOM(notification);
    this.notifications.push(notification);

    // Auto remove after duration
    setTimeout(() => {
      this.remove(notification);
    }, duration);

    // Limit number of notifications
    if (this.notifications.length > this.maxNotifications) {
      const oldest = this.notifications.shift();
      this.remove(oldest);
    }
  }

  createNotification(message, type) {
    const notification = document.createElement("div");

    const config = this.getNotificationConfig(type);

    notification.className = `fixed top-4 right-4 ${config.bgColor} text-white px-6 py-4 rounded-lg shadow-lg z-50 transform transition-all duration-300 translate-x-full`;
    notification.innerHTML = `
      <div class="flex items-center">
        <i class="${config.icon} mr-3 text-lg"></i>
        <span class="font-medium">${message}</span>
        <button onclick="notificationManager.remove(this.closest('.fixed'))" class="ml-4 text-white hover:text-gray-200 transition-colors">
          <i class="fas fa-times"></i>
        </button>
      </div>
    `;

    return notification;
  }

  getNotificationConfig(type) {
    const configs = {
      success: {
        bgColor: "bg-green-500",
        icon: "fas fa-check-circle",
      },
      error: {
        bgColor: "bg-red-500",
        icon: "fas fa-exclamation-circle",
      },
      warning: {
        bgColor: "bg-yellow-500",
        icon: "fas fa-exclamation-triangle",
      },
      info: {
        bgColor: "bg-blue-500",
        icon: "fas fa-info-circle",
      },
    };

    return configs[type] || configs.info;
  }

  addToDOM(notification) {
    document.body.appendChild(notification);

    // Animate in
    setTimeout(() => {
      notification.classList.remove("translate-x-full");
    }, 100);
  }

  remove(notification) {
    if (!notification || !notification.parentNode) return;

    notification.classList.add("translate-x-full");

    setTimeout(() => {
      if (notification.parentNode) {
        notification.parentNode.removeChild(notification);
      }

      // Remove from array
      const index = this.notifications.indexOf(notification);
      if (index > -1) {
        this.notifications.splice(index, 1);
      }
    }, 300);
  }

  // Convenience methods
  success(message, duration = 5000) {
    this.show(message, "success", duration);
  }

  error(message, duration = 7000) {
    this.show(message, "error", duration);
  }

  warning(message, duration = 6000) {
    this.show(message, "warning", duration);
  }

  info(message, duration = 5000) {
    this.show(message, "info", duration);
  }

  // Clear all notifications
  clearAll() {
    this.notifications.forEach((notification) => {
      this.remove(notification);
    });
  }
}

// Global instance
const notificationManager = new NotificationManager();

// Global functions for backward compatibility
function showNotification(message, type = "info", duration = 5000) {
  notificationManager.show(message, type, duration);
}

function closeNotification(button) {
  const notification = button.closest(".fixed.top-4.right-4");
  notificationManager.remove(notification);
}

// Export for module systems
if (typeof module !== "undefined" && module.exports) {
  module.exports = { NotificationManager, notificationManager };
}
