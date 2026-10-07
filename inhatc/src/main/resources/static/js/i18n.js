(function () {
  "use strict";

  var STORAGE_KEY = "sns-lang";

  var MESSAGES = {
    ko: {
      "nav.home": "Home",
      "nav.user": "User",
      "nav.message": "Message",
      "nav.alarm": "Alarm",
      "nav.logout": "Logout",
      "nav.settings": "설정",
      "nav.language": "언어",
      "nav.theme": "테마",
      "nav.themeDark": "다크 모드",
      "lang.ko": "한국어",
      "lang.ja": "日本語",
      "feed.forYou": "추천",
      "feed.following": "팔로잉",
      "feed.composePlaceholder": "무슨 일이 일어나고 있나요?",
      "feed.post": "게시",
      "feed.posting": "게시 중…",
      "mypage.followers": "팔로워",
      "mypage.following": "팔로잉",
      "mypage.follow": "팔로우",
      "mypage.followingBtn": "팔로잉",
      "mypage.message": "메시지",
      "mypage.emptyPosts": "작성한 게시글이 없습니다.",
      "mypage.editPost": "게시물 수정",
      "mypage.editPlaceholder": "게시물 내용",
      "mypage.cancel": "취소",
      "mypage.save": "저장",
      "mypage.edit": "수정",
      "mypage.delete": "삭제",
      "mypage.postMenu": "게시물 메뉴",
      "mypage.bannerChange": "배경 변경",
      "post.title": "게시물",
      "post.loading": "게시물을 불러오는 중...",
      "post.comments": "댓글",
      "post.commentPlaceholder": "댓글을 입력하세요",
      "post.commentSubmit": "댓글 등록",
      "auth.login": "로그인",
      "auth.signup": "가입하기",
      "auth.email": "이메일",
      "auth.password": "비밀번호",
      "auth.name": "이름",
      "auth.show": "표시",
      "auth.hide": "숨김",
      "auth.noAccount": "계정이 없으신가요?",
      "auth.hasAccount": "이미 계정이 있으신가요?",
      "auth.signupLink": "가입하기",
      "auth.loginLink": "로그인",
      "auth.createAccount": "계정 만들기",
      "auth.loginFailed": "이메일 또는 비밀번호가 올바르지 않습니다.",
      "auth.loggingIn": "로그인 중…",
      "auth.loginRequired": "로그인이 필요합니다.",
      "notif.title": "알림",
      "notif.viewAll": "전체 보기",
      "notif.empty": "새로운 알림이 없습니다.",
      "notif.loading": "알림을 불러오는 중...",
      "notif.loadFailed": "알림을 불러올 수 없습니다.",
      "notif.like": "좋아요를 눌렀습니다",
      "notif.comment": "댓글을 남겼습니다",
      "notif.likeType": "❤️ 좋아요",
      "notif.commentType": "💬 댓글",
      "notif.actorParticle": "님이",
      "msg.title": "메시지",
      "msg.send": "전송",
      "msg.loading": "메시지를 불러오는 중...",
      "msg.conversationsLoading": "대화 상대를 불러오는 중...",
      "msg.placeholder": "메시지를 입력하세요...",
      "msg.imageUpload": "이미지 업로드",
      "msg.backToList": "목록으로",
      "common.backMain": "메인으로",
      "follow.unfollow": "언팔로우",
      "follow.follow": "팔로우",
      "follow.following": "팔로잉",
      "mypage.followListEmpty": "목록이 비어있습니다.",
      "common.close": "닫기",
    },
    ja: {
      "nav.home": "ホーム",
      "nav.user": "ユーザー",
      "nav.message": "メッセージ",
      "nav.alarm": "通知",
      "nav.logout": "ログアウト",
      "nav.settings": "設定",
      "nav.language": "言語",
      "nav.theme": "テーマ",
      "nav.themeDark": "ダークモード",
      "lang.ko": "한국어",
      "lang.ja": "日本語",
      "feed.forYou": "おすすめ",
      "feed.following": "フォロー中",
      "feed.composePlaceholder": "いまどうしてる？",
      "feed.post": "投稿",
      "feed.posting": "投稿中…",
      "mypage.followers": "フォロワー",
      "mypage.following": "フォロー中",
      "mypage.follow": "フォロー",
      "mypage.followingBtn": "フォロー中",
      "mypage.message": "メッセージ",
      "mypage.emptyPosts": "投稿がありません。",
      "mypage.editPost": "投稿を編集",
      "mypage.editPlaceholder": "投稿内容",
      "mypage.cancel": "キャンセル",
      "mypage.save": "保存",
      "mypage.edit": "編集",
      "mypage.delete": "削除",
      "mypage.postMenu": "投稿メニュー",
      "mypage.bannerChange": "背景を変更",
      "post.title": "投稿",
      "post.loading": "投稿を読み込み中...",
      "post.comments": "コメント",
      "post.commentPlaceholder": "コメントを入力",
      "post.commentSubmit": "コメント投稿",
      "auth.login": "ログイン",
      "auth.signup": "登録",
      "auth.email": "メール",
      "auth.password": "パスワード",
      "auth.name": "名前",
      "auth.show": "表示",
      "auth.hide": "非表示",
      "auth.noAccount": "アカウントをお持ちでないですか？",
      "auth.hasAccount": "すでにアカウントをお持ちですか？",
      "auth.signupLink": "登録",
      "auth.loginLink": "ログイン",
      "auth.createAccount": "アカウント作成",
      "auth.loginFailed": "メールアドレスまたはパスワードが正しくありません。",
      "auth.loggingIn": "ログイン中…",
      "auth.loginRequired": "ログインが必要です。",
      "notif.title": "通知",
      "notif.viewAll": "すべて見る",
      "notif.empty": "新しい通知はありません。",
      "notif.loading": "通知を読み込み中...",
      "notif.loadFailed": "通知を読み込めません。",
      "notif.like": "いいねしました",
      "notif.comment": "コメントしました",
      "notif.likeType": "❤️ いいね",
      "notif.commentType": "💬 コメント",
      "notif.actorParticle": "さんが",
      "msg.title": "メッセージ",
      "msg.send": "送信",
      "msg.loading": "メッセージを読み込み中...",
      "msg.conversationsLoading": "会話相手を読み込み中...",
      "msg.placeholder": "メッセージを入力...",
      "msg.imageUpload": "画像アップロード",
      "msg.backToList": "一覧へ",
      "common.backMain": "メインへ",
      "follow.unfollow": "フォロー解除",
      "follow.follow": "フォロー",
      "follow.following": "フォロー中",
      "mypage.followListEmpty": "リストは空です。",
      "common.close": "閉じる",
    },
  };

  function getStoredLang() {
    try {
      var lang = localStorage.getItem(STORAGE_KEY) || "ko";
      return lang === "ja" ? "ja" : "ko";
    } catch (e) {
      return "ko";
    }
  }

  function t(key, lang) {
    var l = lang || getStoredLang();
    if (MESSAGES[l] && MESSAGES[l][key]) return MESSAGES[l][key];
    if (MESSAGES.ko[key]) return MESSAGES.ko[key];
    return key;
  }

  function applyLang(lang) {
    var next = lang === "ja" ? "ja" : "ko";
    document.documentElement.setAttribute("lang", next);
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch (e) {
      /* ignore */
    }

    document.querySelectorAll("[data-i18n]").forEach(function (el) {
      var key = el.getAttribute("data-i18n");
      var value = t(key, next);
      var attr = el.getAttribute("data-i18n-attr");
      if (attr) {
        el.setAttribute(attr, value);
      } else if (el.tagName === "INPUT" || el.tagName === "TEXTAREA") {
        if (el.hasAttribute("placeholder") || el.getAttribute("data-i18n-target") === "placeholder") {
          el.placeholder = value;
        } else {
          el.textContent = value;
        }
      } else {
        el.textContent = value;
      }
    });

    document.querySelectorAll("[data-i18n-placeholder]").forEach(function (el) {
      el.placeholder = t(el.getAttribute("data-i18n-placeholder"), next);
    });

    document.querySelectorAll("[data-i18n-error]").forEach(function (el) {
      el.textContent = t(el.getAttribute("data-i18n-error"), next);
    });

    document.querySelectorAll(".lang-switcher-btn").forEach(function (btn) {
      var btnLang = btn.getAttribute("data-lang");
      btn.classList.toggle("active", btnLang === next);
      btn.setAttribute("aria-pressed", btnLang === next ? "true" : "false");
    });

    document.dispatchEvent(new CustomEvent("sns:langchange", { detail: { lang: next } }));
  }

  function bindLangSwitcher(wrap) {
    if (!wrap || wrap.dataset.bound) return;
    wrap.dataset.bound = "1";
    wrap.addEventListener("click", function (e) {
      var btn = e.target.closest(".lang-switcher-btn");
      if (!btn) return;
      applyLang(btn.getAttribute("data-lang"));
    });
  }

  function createLangSwitcher() {
    var wrap = document.createElement("div");
    wrap.id = "langSwitcher";
    wrap.className = "lang-switcher";
    wrap.innerHTML =
      '<button type="button" class="lang-switcher-btn" data-lang="ko" aria-pressed="false">한국어</button>' +
      '<button type="button" class="lang-switcher-btn" data-lang="ja" aria-pressed="false">日本語</button>';
    bindLangSwitcher(wrap);
    return wrap;
  }

  function ensureLangSwitcher() {
    var existing = document.getElementById("langSwitcher");
    if (existing) {
      bindLangSwitcher(existing);
      return;
    }

    var wrap = createLangSwitcher();

    var settingsPanel = document.getElementById("sidebarSettingsPanel");
    if (settingsPanel) {
      var langSection = settingsPanel.querySelector(".sidebar-settings-section:last-child");
      if (langSection) {
        langSection.appendChild(wrap);
        return;
      }
    }

    var feedHeader = document.querySelector(".feed-page-header");
    if (feedHeader) {
      feedHeader.appendChild(wrap);
      return;
    }

    var tabs = document.querySelector(".main-content > .top-tabs, .feed-column > .top-tabs");
    if (tabs && !tabs.closest(".feed-page-header")) {
      var header = document.createElement("div");
      header.className = "feed-page-header";
      tabs.parentNode.insertBefore(header, tabs);
      header.appendChild(tabs);
      header.appendChild(wrap);
      return;
    }

    var pageHeader = document.querySelector(".main-content > .top-header, .main-content > .chat-header");
    if (pageHeader) {
      var toolbar = pageHeader.querySelector(".header-toolbar");
      if (!toolbar) {
        toolbar = document.createElement("div");
        toolbar.className = "header-toolbar";
        pageHeader.appendChild(toolbar);
      }
      toolbar.appendChild(wrap);
      return;
    }

    var notifHeader = document.querySelector(".notif-page .page-header");
    if (notifHeader) {
      var actions = notifHeader.querySelector(".page-header-actions");
      if (!actions) {
        actions = document.createElement("div");
        actions.className = "page-header-actions";
        notifHeader.appendChild(actions);
      }
      actions.appendChild(wrap);
      return;
    }

    var wrapper = document.querySelector(".wrapper");
    if (wrapper) {
      wrap.classList.add("lang-switcher--fallback");
      wrapper.appendChild(wrap);
      return;
    }

    document.body.appendChild(wrap);
  }

  function followLabel(isFollowing, hoverUnfollow) {
    if (isFollowing) {
      return hoverUnfollow ? t("follow.unfollow") : t("mypage.followingBtn");
    }
    return t("mypage.follow");
  }

  document.addEventListener("DOMContentLoaded", function () {
    ensureLangSwitcher();
    applyLang(getStoredLang());
  });

  window.SnsI18n = {
    t: t,
    applyLang: applyLang,
    getLang: getStoredLang,
    followLabel: followLabel,
  };

  applyLang(getStoredLang());
})();
