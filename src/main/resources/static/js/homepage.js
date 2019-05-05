let canvas;
let ctx;

/**
 * When the login is attempted, saves username to cookies.
 */
$(".login-push").submit(function () {
    let cookie = "";
    cookie += "username=";
    cookie += ($("#username").val()).toString();
    cookie += "; expires Thu, 21 Aug 2099 20:00:00 UTC; path=/ ";
    document.cookie = cookie;
});

/**
 * When logout is submitted, deletes username and userid cookie data.
 */
$("#logout").submit(function () {
    document.cookie = "username=; expires Thu, 21 Aug 2010 20:00:00 UTC";
    document.cookie = "userid=; expires Thu, 21 Aug 2010 20:00:00 UTC";

});