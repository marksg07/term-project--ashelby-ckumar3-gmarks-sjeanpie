let canvas;
let ctx;
let playerPaddle;
let oppRightPaddle;
let oppLeftPaddle;
let ballLeft;
let ballRight;




$(document).ready(() => {
    // Setting up the canvas.  Already has a width and height.
    canvas = $('#pong-canvas')[0];
// Set up the canvas context.
    ctx = canvas.getContext("2d");
//route on search params
    $searchButton.on('click', createRoute);
// Load a db on click and then make a map
    $loadButton.on('click', loadMap);
// CLICK HANDLER FOR THE CANVAS -- nearest function
    canvas.addEventListener('click', nearest);
// Timer for the navigation (zooming, panning)
    navTimer = 0;
    setInterval(function(){ navTimer += 50;}, 50);

// SCROLL HANDLER FOR THE CANVAS
    canvas.addEventListener('mousewheel', mouseWheelHandler, false);

// PANNING HANDLER
    canvas.addEventListener('mousedown', onMouseDown, false);
    canvas.addEventListener('mouseup', onMouseUp, false);
});