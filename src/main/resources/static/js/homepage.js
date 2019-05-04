let canvas;
let ctx;

$(document).ready(() => {
    // Setting up the canvas.  Already has a width and height.
    canvas = $('#pong-canvas')[0];
    // Set up the canvas context.
    // ctx = canvas.getContext("2d");
    // ctx.fillRect(0, 0, canvas.width, canvas.height);
	// ctx.font = "50px Futura, sans-serif";
    // ctx.fillStyle = "white";
	// ctx.fillText("P O N G F O L K S", canvas.width / 4.5, canvas.height/2);
	// ctx.textAlign = "center";
	$("#find-game").click(test);
});

function test() {
    $.get("game", function() {
            console.log('test');
        }
    );
}

$(".login-push").submit(function() {
    console.log($("#remember-user")[0].checked);
    if ($("#remember-user")[0].checked) {
        console.log("X");
        console.log("username=" + $("#username").val());
        console.log("; password=" + $("#login-pass").val());
        console.log("X");
        let cookie = "";
        cookie += "username=";
        cookie += ($("#username").val()).toString();
        cookie += "; expires Thu, 21 Aug 2099 20:00:00 UTC; path=/ ";
        document.cookie = cookie;
        cookie = "";
        cookie += "password=";
        cookie += ($("#login-pass").val()).toString();
        cookie += "; expires Thu, 21 Aug 2099 20:00:00 UTC; path=/ ";
        document.cookie = cookie;
    } else {
        console.log("UNCHECKED");
        document.cookie = "username=; expires Thu, 21 Aug 2010 20:00:00 UTC";
        document.cookie = "password=; expires Thu, 21 Aug 2010 20:00:00 UTC";
    }
});
