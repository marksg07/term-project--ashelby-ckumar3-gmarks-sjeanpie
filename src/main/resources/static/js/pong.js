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
let up = false;
let down = false;

let playersRemaining;
/**
 * Gets new position data from server and updates positions of all entities.
 */
function updatePositions() {
    const postParameters = {upPressed: up,
        downPressed: down};

    $.post("/updatePositions", postParameters, responseJSON => {
        //Parse the JSON response into a JavaScript object.
        const responseObject = JSON.parse(responseJSON);
        oppLeftPaddle.setPosition(responseObject.leftPaddleY);
        playerPaddle.setPosition(responseObject.playerPaddleY);
        oppRightPaddle.setPosition(responseObject.rightPaddleY);
        ballLeft.setPosition(responseObject.ballLeftX, responseObject.ballLeftY);
        ballLeft.setPosition(responseObject.ballRightX, responseObject.ballRightY);

        up = false;
        down = false;

});
}

/**
 * tracks time when up/down is pressed.
 * @param e
 */
function checkPressed(e) {
    if (e.which == 38) {
        up = true;
        return;
    }
    if (e.which == 40) {
        down = true;
        return;
    }
    return;
}

/**
 * checks for inputs and updates paddle coords, will send to back end in future
 * @param e
 */
function guessUpdate(e) {
    if (up) {
        playerPaddle.setPosition(y-1);
        return;
    }
    if (down) {
        playerPaddle.setPosition(y+1);
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
    up = false;
    down = false;
    playerPaddle = new Paddle(canvas.width/2, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    oppLeftPaddle = new Paddle(0, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    oppRightPaddle = new Paddle(canvas.width-paddleWidth, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    ballLeft = new Ball(20, ctx, (canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
    ballRight = new Ball(20, ctx, (3*canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
    canvas.addEventListener("onkeydown", checkPressed, false);
    canvas.setInterval(updatePositions, 20);
});