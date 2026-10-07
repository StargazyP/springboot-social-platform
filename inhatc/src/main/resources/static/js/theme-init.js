(function () {
  "use strict";
  var KEY = "sns-theme";
  try {
    var stored = localStorage.getItem(KEY);
    var theme = stored === "dark" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", theme);
  } catch (e) {
    document.documentElement.setAttribute("data-theme", "light");
  }
})();
