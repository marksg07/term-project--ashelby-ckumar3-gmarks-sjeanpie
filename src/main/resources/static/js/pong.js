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
let upDown = [0,0];
/**
 * Gets new position data from server and updates positions of all entities.
 */
function updatePositions() {
    console.log(pressState());
    const postParameters = {press : pressState()};
    $.post("/logic", postParameters, responseJSON => {
        //Parse the JSON response into a JavaScript object.
        const responseObject = JSON.parse(responseJSON);
        // console.log(responseObject.playerPaddleY);
        // console.log(responseObject.leftPaddleY);
        // console.log(responseObject.rightPaddleY);
        // console.log(responseObject);
        oppLeftPaddle.setPosition(responseObject.leftPaddleY);
        playerPaddle.setPosition(responseObject.playerPaddleY);
        oppRightPaddle.setPosition(responseObject.rightPaddleY);
        ballLeft.setPosition(responseObject.ballLeftX, responseObject.ballLeftY);
        ballRight.setPosition(responseObject.ballRightX, responseObject.ballRightY);


});
}

/**
 * tracks time when up/down is pressed.
 * @param e
 */
function checkPressed(e) {
    if ((e.which === 38) || (e.which === 87)) {
        upDown[0] = 2;
        if (upDown[1] === 2){
            upDown[1] = 1;
        }
        return;
    }
    if ((e.which === 40) || (e.which === 83)) {
        upDown[1] = 2;
        if (upDown[0] === 2){
            upDown[0] = 1;
        }
        return;
    }
    return;
}

/**
 * turns off keypress indicator
 */
function checkUp(e) {
    if ((e.which === 38) || (e.which === 87)) {
        upDown[0] = 0;
        return;
    }
    if ((e.which === 40) || (e.which === 83)) {
        upDown[1] = 0;
        return;
    }
    return;
}

/**
 * gets pressState
 */
function pressState() {
    if (upDown[0] > upDown[1]) {
        return 1;
    } else if (upDown[0] < upDown[1]) {
        return -1;
    } else {
        return 0;
    }
}


/**
 * checks for inputs and updates paddle coords, will send to back end in future
 * @param e
 */
function checkInputs(e) {
    if (up) {
        playerPaddle.setPosition(y-1);
        up = false;
        return;
    }
    if (down) {
        playerPaddle.setPosition(y+1);
        down = false;
        return;
    }
    return;
}




$(document).ready(() => {
    // Setting up the canvas.  Already has a width and height.
    canvas = $('#pong-canvas')[0];
    // Set up the canvas context.
    ctx = canvas.getContext("2d");
    ctx.fillStyle = "black";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    //initial width of paddle, will be a constant
    paddleWidth = 10;
    //initial height of paddles, another constant
    paddleHeight = 40;
    //initial ball size, another constant
    ballSize = 20;
    //initialize 5 entities.
    up = false;
    down = false;
    playerPaddle = new Paddle(canvas.width/2, canvas.height/2-(paddleHeight/2), paddleWidth, paddleHeight, ctx);
    oppLeftPaddle = new Paddle(0, canvas.height/2-(paddleHeight/2), paddleWidth, paddleHeight, ctx);
    oppRightPaddle = new Paddle(canvas.width-paddleWidth, canvas.height/2-(paddleHeight/2), paddleWidth, paddleHeight, ctx);
    ballLeft = new Ball(20, ctx, (canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
    ballRight = new Ball(20, ctx, (3*canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
    $(document).keydown(event => {checkPressed(event);});
    $(document).keyup(even => {checkUp(event);});
    setInterval(updatePositions, 20);
});