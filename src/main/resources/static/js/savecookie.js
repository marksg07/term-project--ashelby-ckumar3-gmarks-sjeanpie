$(document).ready(() => {
    let cookie = ("userid=" + $("#userid").val() + "; expires Thu, 21 Aug 2099 20:00:00 UTC; path=/ ");
    document.cookie = cookie;
});