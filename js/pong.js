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

/**
 * Gets new position data from server and updates positions of all entities.
 */
function updatePositions() {
    $.post("/updatePositions", postParameters, responseJSON => {
        //Parse the JSON response into a JavaScript object.
        const responseObject = JSON.parse(responseJSON);
        oppLeftPaddle.setPosition(responseObject.leftPaddleY);
        playerPaddle.setPosition(responseObject.playerPaddleY);
        oppRightPaddle.setPosition(responseObject.rightPaddleY);
        ballLeft.setPosition(responseObject.ballLeftX, responseObject.ballLeftY);
        ballLeft.setPosition(responseObject.ballRightX, responseObject.ballRightY);

});
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
    playerPaddle = new Paddle(canvas.width/2, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    oppLeftPaddle = new Paddle(0, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    oppRightPaddle = new Paddle(canvas.width-paddleWidth, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    ballLeft = new Ball(20, ctx, (canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
    ballRight = new Ball(20, ctx, (3*canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
});