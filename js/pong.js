let canvas;
let ctx;
let playerPaddle;
let oppRightPaddle;
let oppLeftPaddle;
let ballLeft;
let ballRight;

let paddleWidth;
let paddleHeight;
let ballSize;

let upTimePressed;
let downTimePressed;
let totalUpTime;
let totalDownTime;
let up;
let down;
/**
 * Gets new position data from server and updates positions of all entities.
 */
function updatePositions() {
    $.post("/updatePositions", responseJSON => {
        //Parse the JSON response into a JavaScript object.
        const responseObject = JSON.parse(responseJSON);
        oppLeftPaddle.setPosition(responseObject.leftPaddleY);
        playerPaddle.setPosition(responseObject.playerPaddleY);
        oppRightPaddle.setPosition(responseObject.rightPaddleY);
        ballLeft.setPosition(responseObject.ballLeftX, responseObject.ballLeftY);
        ballLeft.setPosition(responseObject.ballRightX, responseObject.ballRightY);

});
}

/**
 * tracks time when up/down is pressed.
 * @param e
 */
function startTime(e) {
    if (e.which == 38) {
        up = true;
        upTimePressed = e.timeStamp;
        return;
    }
    if (e.which == 40) {
        down = true;
        downTimePressed = e.timeStamp;
        return;
    }
    return;
}

/**
 * adds total time pressed of each key
 * @param e
 */
function endTime(e) {
    if (up) {
        let timePressed = e.timeStamp - upTimePressed;
        totalUpTime += timePressed;
        upTimePressed = 0;
        return;
    }
    if (down) {
        let timePressed = e.timeStamp - downTimePressed;
        totalDownTime += timePressed;
        downTimePressed = 0;
        return;
    }
    return;
}

$(document).ready(() => {
    // Setting up the canvas.  Already has a width and height.
    canvas = $('#pong-canvas')[0];
    // Set up the canvas context.
    ctx = canvas.getContext("2d");
    //initial width of paddle, will be a constant
    paddleWidth = 10;
    //initial height of paddles, another constant
    paddleHeight = 40;
    //initial ball size, another constant
    ballSize = 20;
    //initialize 5 entities.
    totalUpTime = 0;
    totalDownTime = 0;
    up = false;
    down = false;
    playerPaddle = new Paddle(canvas.width/2, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    oppLeftPaddle = new Paddle(0, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    oppRightPaddle = new Paddle(canvas.width-paddleWidth, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    ballLeft = new Ball(20, ctx, (canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
    ballRight = new Ball(20, ctx, (3*canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
    canvas.addEventListener("onkeydown", startTime, false);
    canvas.addEventListener("onkeyup", endTime, false);
});