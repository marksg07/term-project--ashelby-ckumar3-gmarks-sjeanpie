let playerPaddle;
let oppRightPaddle;
let oppLeftPaddle;
let ballLeft;
let ballRight;


const paddleWidth = 10;
const paddleHeight = 40;
const ballSize = 20;
const gameScreenTime = 3.8;
let up = false;
let down = false;
let playersRemaining;
let upDown = [0,0];
let gameReady = false;
let leftGameBegun = false;
let rightGameBegun = false;
let gameOver;
const leftSec = $("#leftCountdown");
const rightSec = $("#rightCountdown");

let leftBoardState = 'none';
let rightBoardState = 'none';
let killLog = [];
let logStartPointer = 0;
let killLogElement;


function setGameReady(v) {
    gameReady = v;
}

function setLeftBoardState(st) {
    if (st === leftBoardState)
        return;
    leftBoardState = st;
    if (st === 'red') {
        ctx.fillStyle = 'red';
        ctx.fillRect(0, 0, canvas.width / 2 - 20, canvas.height);

    } else if (st === 'none') {
        ctx.fillStyle = 'black';
        ctx.fillRect(0, 0, canvas.width / 2 - 20, canvas.height);
        oppLeftPaddle.draw();
        ballLeft.draw();
    }
}

function setRightBoardState(st) {
    if (st === rightBoardState)
        return;
    rightBoardState = st;
    if (st === 'red') {
        ctx.fillStyle = 'red';
        ctx.fillRect(canvas.width/2 + 20, 0, canvas.width, canvas.height);
    } else if (st === 'none') {
        ctx.fillStyle = 'black';
        ctx.fillRect(canvas.width/2 + 20, 0, canvas.width, canvas.height);
        oppRightPaddle.draw();
        ballRight.draw();
    }
}

function updateGame(state) {
    if(gameOver) {
        return;
    }
    // console.log(state);
    if(!state.hasOwnProperty("left") && !state.hasOwnProperty("right")) {
        // i am dead :(
        return;
    }

    if (state.left === "dead") {
        leftSec.hide();
        oppLeftPaddle.hide();
        ballLeft.hide();
        setLeftBoardState('red');
    }
    else if (state.left.hasOwnProperty("cdSecondsLeft")) {
        if(state.left.cdSecondsLeft > gameScreenTime && leftGameBegun) {
            setLeftBoardState('red');
        }
        else {
            if(state.left.cdSecondsLeft <= gameScreenTime) {
                leftGameBegun = true;
            }
            setLeftBoardState('none')
            leftSec.show();
            const secString = state.left.cdSecondsLeft.toFixed(1);
            leftSec.text(secString);
        }
    } else {
        // console.log("left game live");
        setLeftBoardState('none');
        leftSec.hide();
        oppLeftPaddle.setPosition(state.left.p1PaddleY);
        playerPaddle.setPosition(state.left.p2PaddleY);
        ballLeft.setPosition(state.left.ballX + paddleWidth, state.left.ballY);
    }

    if (state.right === "dead") {
        rightSec.hide();
        oppRightPaddle.hide();
        ballRight.hide();
        setRightBoardState('red');
    } else if (state.right.hasOwnProperty("cdSecondsLeft")) {
        if(state.right.cdSecondsLeft > gameScreenTime && rightGameBegun) {
            setRightBoardState('red');
        }
        else {
            if (state.right.cdSecondsLeft <= gameScreenTime) {
                rightGameBegun = true;
            }
            setRightBoardState('none');
            rightSec.show();
            const secString = state.right.cdSecondsLeft.toFixed(1);
            rightSec.text(secString);
        }
    } else {
        setRightBoardState('none');
        // console.log("right game live");
        rightSec.hide();
        oppRightPaddle.setPosition(state.right.p2PaddleY);
        playerPaddle.setPosition(state.right.p1PaddleY);
        ballRight.setPosition(state.right.ballX + canvas.width / 2 + paddleWidth / 2, state.right.ballY);
    }

}

function sendInput() {
    conn.send(JSON.stringify({"type": MESSAGE_TYPE.INPUT, "payload": {"id": myId, "input": pressState()}}));
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

let inputHandle;

function onPlayerDead() {
    // XXX
    gameOver = true;
    clearInterval(inputHandle);
    ctx.fillStyle = "red";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    $("#status").text("ded");
    conn.close();
}

function onPlayerWin() {
    gameOver = true;
    clearInterval(inputHandle);
    ctx.fillStyle = "green";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    $("#status").text("ur winner");
    conn.close();
}

function setUsers(left, right) {
    $("#leftName").text(left);
    $("#rightName").text(right);
}

function getVerb() {
    return "ponged";
}

function logEntryElement(killer, killed) {
    let newDiv = document.createElement("div");
    newDiv.className = "logEntry";
    if(killer !== "") {
        newDiv.innerHTML = killer + " <strong>" + getVerb() + "</strong> " + killed;
    } else {
        newDiv.innerHTML = killed + " <strong>disconnected</strong>"
    }
    return newDiv;
}

function addToKillLog(killer, killed) {
    killLog.push([killer, killed, (new Date()).getTime()]);
    killLogElement.appendChild(logEntryElement(killer, killed));
}

const logTime = 5;

function drawKillLog() {
    const earliestLogTime = (new Date()).getTime() - logTime * 1000;
    for(let i = logStartPointer; i < killLog.length; i++) {
        const log = killLog[i];
        if(log[2] < earliestLogTime) {
            logStartPointer = i + 1;
            // Was in the log, must be deleted
            killLogElement.removeChild(killLogElement.firstChild);
        }

    }
}

function executePong() {
    wsSetup();
    // Setting up the canvas.  Already has a width and height.
    canvas = $('#pong-canvas')[0];
    killLogElement = $("#killLog")[0];
    console.log(canvas.width);
    // canvas.width = canvas.width + paddleWidth * 3;
    console.log(canvas.width);
    // Set up the canvas context.
    ctx = canvas.getContext("2d");
    ctx.fillStyle = "black";
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    //initialize 5 entities.
    up = false;
    down = false;
    playerPaddle = new Paddle(canvas.width/2, canvas.height/2, paddleWidth, paddleHeight, ctx);
    oppLeftPaddle = new Paddle(paddleWidth/2, canvas.height/2, paddleWidth, paddleHeight, ctx);
    oppRightPaddle = new Paddle(canvas.width-(paddleWidth/2), canvas.height/2, paddleWidth, paddleHeight, ctx);
    ballLeft = new Ball(ballSize, ctx, (canvas.width/4)-(ballSize/2), canvas.height/2);
    ballRight = new Ball(ballSize, ctx, (3*canvas.width/4)-(ballSize/2), canvas.height/2);
    // ctx.font = "50px Futura, sans-serif";
    // ctx.fillStyle = "white";
    // ctx.textAlign = "center";
    // ctx.fillText("Finding Players.....", canvas.width /2, 4*canvas.height/5);
    $(document).keydown(event => {checkPressed(event);});
    $(document).keyup(event => {checkUp(event);});
    inputHandle = setInterval(sendInput, 20);
    setInterval(drawKillLog, 20);
    $("#name").text(myId);
}

function rmWaitingText() {
    // ctx.fillStyle = "black";
    // ctx.fillRect(0, 4*canvas.height/5, canvas.width, canvas.height);
    $("#status").text("");
}

$(document).ready(() => {
    executePong();
});
