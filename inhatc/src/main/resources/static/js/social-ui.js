(function () {
  "use strict";

  var STORAGE_KEY = "sns-theme";

  function getStoredTheme() {
    try {
      return localStorage.getItem(STORAGE_KEY) || "light";
    } catch (e) {
      return "light";
    }
  }

  function syncToggleInputs(theme) {
    document.querySelectorAll("input[data-theme-toggle]").forEach(function (input) {
      input.checked = theme === "dark";
    });
  }

  function applyTheme(theme) {
    var next = theme === "dark" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", next);
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch (e) {
      /* ignore */
    }
    syncToggleInputs(next);
  }

  function initThemeToggle() {
    applyTheme(getStoredTheme());

    document.addEventListener("change", function (e) {
      if (e.target && e.target.matches("input[data-theme-toggle]")) {
        applyTheme(e.target.checked ? "dark" : "light");
      }
    });
  }

  function initPasswordToggles() {
    document.querySelectorAll("[data-password-toggle]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        var id = btn.getAttribute("data-password-toggle");
        var input = document.getElementById(id);
        if (!input) return;
        var hidden = input.type === "password";
        input.type = hidden ? "text" : "password";
        var showLabel = window.SnsI18n ? window.SnsI18n.t("auth.hide") : "숨김";
        var hideLabel = window.SnsI18n ? window.SnsI18n.t("auth.show") : "표시";
        btn.textContent = hidden ? showLabel : hideLabel;
        btn.setAttribute("aria-label", hidden ? showLabel : hideLabel);
      });
    });
  }

  function initComposeSend() {
    var textarea = document.getElementById("composeContent") || document.getElementById("content");
    var sendBtn = document.getElementById("postSendBtn");
    var counter = document.getElementById("composeCharCount");
    if (!textarea) return;
    var maxLen = parseInt(textarea.getAttribute("maxlength") || "2000", 10);

    function autoGrow() {
      var maxH = 320;
      try {
        var parsed = parseInt(window.getComputedStyle(textarea).maxHeight, 10);
        if (!isNaN(parsed) && parsed > 0) maxH = parsed;
      } catch (e) {}
      textarea.style.height = "auto";
      textarea.style.height = Math.min(textarea.scrollHeight, maxH) + "px";
    }

    function sync() {
      var text = (textarea.value || "").trim();
      var len = (textarea.value || "").length;
      if (counter) counter.textContent = len + " / " + maxLen;
      if (sendBtn) {
        var fileInput = document.getElementById("fileUpload");
        var hasFile = fileInput && fileInput.files && fileInput.files.length > 0;
        sendBtn.disabled = !(text.length > 0 || hasFile);
      }
      autoGrow();
    }

    textarea.addEventListener("input", sync);
    var fileInput = document.getElementById("fileUpload");
    if (fileInput) fileInput.addEventListener("change", sync);
    sync();
  }

  function wrapSavePost() {
    if (typeof window.savePost !== "function") return;
    var original = window.savePost;
    window.savePost = async function (event) {
      var btn = document.getElementById("postSendBtn");
      if (btn) {
        btn.disabled = true;
        btn.dataset.loading = "1";
        btn.textContent = window.SnsI18n ? window.SnsI18n.t("feed.posting") : "게시 중…";
      }
      try {
        await original(event);
      } finally {
        if (btn) {
          btn.dataset.loading = "";
          btn.textContent = window.SnsI18n ? window.SnsI18n.t("feed.post") : "게시";
          initComposeSend();
        }
      }
    };
  }

  function initTabRipple() {
    document.querySelectorAll(".tab-button").forEach(function (tab) {
      tab.addEventListener("click", function () {
        document.querySelectorAll(".tab-button").forEach(function (t) {
          t.classList.remove("active");
          t.setAttribute("aria-selected", "false");
        });
        tab.classList.add("active");
        tab.setAttribute("aria-selected", "true");
      });
    });
  }

  function initFollowButtonHover() {
    document.addEventListener("mouseover", function (e) {
      var btn = e.target.closest && e.target.closest(".follow-btn.following, .follow-item-btn.following");
      if (btn) btn.textContent = window.SnsI18n ? window.SnsI18n.followLabel(true, true) : "언팔로우";
    });
    document.addEventListener("mouseout", function (e) {
      var btn = e.target.closest && e.target.closest(".follow-btn.following, .follow-item-btn.following");
      if (btn) btn.textContent = window.SnsI18n ? window.SnsI18n.followLabel(true, false) : "팔로잉";
    });
  }

  document.addEventListener("sns:langchange", function () {
    syncToggleInputs(getStoredTheme());
    initComposeSend();
    document.querySelectorAll(".follow-btn.following").forEach(function (btn) {
      btn.textContent = window.SnsI18n ? window.SnsI18n.followLabel(true, false) : "팔로잉";
    });
    document.querySelectorAll(".follow-item-btn.following").forEach(function (btn) {
      btn.textContent = window.SnsI18n ? window.SnsI18n.followLabel(true, false) : "팔로잉";
    });
    document.querySelectorAll(".follow-btn:not(.following), .follow-item-btn.follow").forEach(function (btn) {
      btn.textContent = window.SnsI18n ? window.SnsI18n.followLabel(false, false) : "팔로우";
    });
  });

  function initMainFeedMotion() {
    if (!document.body.classList.contains("page-main")) return;

    var container = document.getElementById("postContainer");
    if (!container) return;

    function markCards() {
      var cards = container.querySelectorAll(".card:not(.feed-motion-ready)");
      cards.forEach(function (card, index) {
        card.classList.add("feed-motion-ready");
        card.style.animationDelay = Math.min(index * 0.04, 0.32) + "s";
      });
    }

    markCards();
    var observer = new MutationObserver(markCards);
    observer.observe(container, { childList: true });
  }

  function isEditableTarget(el) {
    if (!el) return false;
    var tag = el.tagName;
    if (tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT") return true;
    if (el.isContentEditable) return true;
    return false;
  }

  function initMainPageScroll() {
    if (!document.body.classList.contains("page-main")) return;

    document.documentElement.style.scrollBehavior = "smooth";

    document.addEventListener("keydown", function (e) {
      if (e.key !== "ArrowDown" && e.key !== "ArrowUp") return;
      if (e.altKey || e.ctrlKey || e.metaKey || e.shiftKey) return;
      if (isEditableTarget(document.activeElement)) return;

      e.preventDefault();
      var step = 100;
      var delta = e.key === "ArrowDown" ? step : -step;
      window.scrollBy({ top: delta, behavior: "smooth" });
    });
  }

  function initSidebarSettings() {
    var btn = document.getElementById("sidebarSettingsBtn");
    var panel = document.getElementById("sidebarSettingsPanel");
    if (!btn || !panel) return;

    function positionPanel() {
      var rect = btn.getBoundingClientRect();
      var top = Math.max(8, rect.top - 24);
      var left = rect.right + 10;
      if (left + 240 > window.innerWidth - 8) {
        left = Math.max(8, rect.left - 230);
      }
      panel.style.top = top + "px";
      panel.style.left = left + "px";
    }

    function closePanel() {
      panel.hidden = true;
      btn.setAttribute("aria-expanded", "false");
      btn.classList.remove("active");
    }

    function openPanel() {
      positionPanel();
      panel.hidden = false;
      btn.setAttribute("aria-expanded", "true");
      btn.classList.add("active");
    }

    window.toggleSettingsPanel = function (event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (panel.hidden) openPanel();
      else closePanel();
    };

    btn.addEventListener("click", function (event) {
      window.toggleSettingsPanel(event);
    });

    document.addEventListener("click", function (e) {
      if (panel.hidden) return;
      if (panel.contains(e.target) || btn.contains(e.target)) return;
      closePanel();
    });

    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && !panel.hidden) closePanel();
      if (e.key === "Escape") {
        const postModal = document.getElementById("postModal");
        if (postModal && postModal.classList.contains("is-open")) {
          if (typeof closeModal === "function") closeModal();
        }
      }
    });

    window.addEventListener("resize", function () {
      if (!panel.hidden) positionPanel();
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    initThemeToggle();
    initPasswordToggles();
    initComposeSend();
    initTabRipple();
    initFollowButtonHover();
    initMainPageScroll();
    initMainFeedMotion();
    initSidebarSettings();
  });

  window.addEventListener("load", function () {
    wrapSavePost();
    initComposeSend();
    syncToggleInputs(getStoredTheme());
  });

  window.SocialUI = {
    applyTheme: applyTheme,
    refreshComposeSend: initComposeSend,
  };

  applyTheme(getStoredTheme());
})();
