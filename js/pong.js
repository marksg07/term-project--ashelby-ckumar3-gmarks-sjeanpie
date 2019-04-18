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

let movingUp;
let movingDown;

//POSTS FROM updatePositions METHOD TO UPDATE THE MODELS.
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
    movingUp = false;
    movingDown = false;
    paddleWidth = 10;
    paddleHeight = 40;
    ballSize = 20;
    playerPaddle = new Paddle(canvas.width/2, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    oppLeftPaddle = new Paddle(0, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    oppRightPaddle = new Paddle(canvas.width-paddleWidth, canvas.height/2-(paddleHeight/2), paddleWdith, paddleHeight, ctx);
    ballLeft = new Ball(20, ctx, (canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
    ballRight = new Ball(20, ctx, (3*canvas.width/4)-(ballSize/2), canvas.height/2-(paddleHeight/2));
});