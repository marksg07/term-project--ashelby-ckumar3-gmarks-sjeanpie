let canvas;
let ctx;

$(document).ready(() => {
    // Setting up the canvas.  Already has a width and height.
    canvas = $('#pong-canvas')[0];
    // Set up the canvas context.
    ctx = canvas.getContext("2d");
    ctx.fillRect(0, 0, canvas.width, canvas.height);
	ctx.font = "50px Futura, sans-serif";
    ctx.fillStyle = "white";
	ctx.fillText("P O N G F O L K S", canvas.width / 4.5, canvas.height/2);
	ctx.textAlign = "center";
	$("#find-game").click(executePong);
});