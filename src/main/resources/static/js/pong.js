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
let upDown = [0, 0];
let leftGameBegun = false;
let rightGameBegun = false;
let gameOver;
const leftSec = $("#leftCountdown");
const rightSec = $("#rightCountdown");
const midSec = $("#midCountdown");

let leftBoardState = 'none';
let rightBoardState = 'none';
let killLog = [];
let logStartPointer = 0;
let killLogElement;

/**
 * Set left boardstate to dead or not.
 * @param st the state of the left board
 */
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

/**
 * Set right boardstate to dead or not.
 * @param st the state of the right board
 */
function setRightBoardState(st) {
    if (st === rightBoardState)
        return;
    rightBoardState = st;
    if (st === 'red') {
        ctx.fillStyle = 'red';
        ctx.fillRect(canvas.width / 2 + 20, 0, canvas.width, canvas.height);
    } else if (st === 'none') {
        ctx.fillStyle = 'black';
        ctx.fillRect(canvas.width / 2 + 20, 0, canvas.width, canvas.height);
        oppRightPaddle.draw();
        ballRight.draw();
    }
}

/**
 * Displays pre-BR timer before the battle royale begins.
 * @param seconds time left until game start
 */
function displayTimer(seconds) {
    midSec.show();
    const secString = "Enough clients to start game. If no more clients join game will start in: " + (seconds + 20).toFixed(1) + " seconds";
    midSec.text(secString);
}

/**
 * Updates the game state to ball and paddle correct positions.
 * @param state data incoming from servers
 */
function updateGame(state) {
    if (state.hasOwnProperty("timeUntilStart")) {
        // if game not yet started
        displayTimer(state.timeUntilStart);
        return;
    } else {
        // if game started, hide center timer
        midSec.hide();
    }
    if (gameOver) {
        // if game over, stop
        return;
    }
    if (!state.hasOwnProperty("left") && !state.hasOwnProperty("right")) {
        // if game has no left and no right, stop
        return;
    }

    if (state.left === "dead") {
        // if left player dead, set left board red and hide left ball and paddle
        leftSec.hide();
        oppLeftPaddle.hide();
        ballLeft.hide();
        setLeftBoardState('red');
    } else if (state.left.hasOwnProperty("cdSecondsLeft")) {
        // if left game starting soon, show red or timer appropriately
        if (state.left.cdSecondsLeft > gameScreenTime && leftGameBegun) {
            setLeftBoardState('red');
        } else {
            if (state.left.cdSecondsLeft <= gameScreenTime) {
                leftGameBegun = true;
            }
            setLeftBoardState('none')
            leftSec.show();
            const secString = state.left.cdSecondsLeft.toFixed(1);
            leftSec.text(secString);
        }
    } else {
        // the left game is being played as normal
        setLeftBoardState('none');
        leftSec.hide();
        oppLeftPaddle.setPosition(state.left.p1PaddleY);
        playerPaddle.setPosition(state.left.p2PaddleY);
        ballLeft.setPosition(state.left.ballX + paddleWidth, state.left.ballY);
    }

    if (state.right === "dead") {
        // if right player dead, set right board red and hide right ball and paddle
        rightSec.hide();
        oppRightPaddle.hide();
        ballRight.hide();
        setRightBoardState('red');
    } else if (state.right.hasOwnProperty("cdSecondsLeft")) {
        // if right game starting soon, show red or timer appropriately
        if (state.right.cdSecondsLeft > gameScreenTime && rightGameBegun) {
            setRightBoardState('red');
        } else {
            if (state.right.cdSecondsLeft <= gameScreenTime) {
                rightGameBegun = true;
            }
            setRightBoardState('none');
            rightSec.show();
            const secString = state.right.cdSecondsLeft.toFixed(1);
            rightSec.text(secString);
        }
    } else {
        // the right game is being played as normal
        setRightBoardState('none');
        rightSec.hide();
        oppRightPaddle.setPosition(state.right.p2PaddleY);
        playerPaddle.setPosition(state.right.p1PaddleY);
        ballRight.setPosition(state.right.ballX + canvas.width / 2 + paddleWidth / 2, state.right.ballY);
    }

}

/**
 * Sends input to the server.
 */
function sendInput() {
    conn.send(JSON.stringify({"type": MESSAGE_TYPE.INPUT, "payload": {"id": myId, "input": pressState()}}));
}

/**
 * Tracks time when up/down is pressed. Interacts correctly with
 * holding down buttons simultaneously and in various order.
 * @param e
 */
function checkPressed(e) {
    if ((e.which === 38) || (e.which === 87)) {
        upDown[0] = 2;
        if (upDown[1] === 2) {
            upDown[1] = 1;
        }
        return;
    }
    if ((e.which === 40) || (e.which === 83)) {
        upDown[1] = 2;
        if (upDown[0] === 2) {
            upDown[0] = 1;
        }
        return;
    }
    return;
}

/**
 * Turns off keypress indicator.
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
 * Gets pressState.
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
 * Checks for inputs and updates paddle coords, will send to back end in future.
 * @param e
 */
function checkInputs(e) {
    if (up) {
        playerPaddle.setPosition(y - 1);
        up = false;
        return;
    }
    if (down) {
        playerPaddle.setPosition(y + 1);
        down = false;
        return;
    }
    return;
}

let inputHandle;

/**
 * Handles when player dies.
 */
function onPlayerDead() {
    gameOver = true;
    clearInterval(inputHandle);
    ctx.fillStyle = "red";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    $("#status").text("ded");
    conn.close();
    $("#new-game-form")[0].hidden = false;
}

/**
 * Handles when player wins.
 */
function onPlayerWin() {
    gameOver = true;
    clearInterval(inputHandle);
    ctx.fillStyle = "green";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    $("#status").text("ur winner");
    conn.close();
    $("#new-game-form")[0].hidden = false;
}

/**
 * Sets opponent name fields.
 * @param left left opponent username
 * @param right right opponent username
 */
function setUsers(left, right) {
    $("#leftName").text(left);
    $("#rightName").text(right);
}

const verbs = ["ponged", "yote", "vanquished", "has slain", "terminated", "erased", "balled", "smote", "bonked", "sank", "trounced", "trampled", "got"];
/**
 * Returns one appropriate verb for when a player is eliminated.
 * @returns {string}
 */
function getVerb() {
    return verbs[Math.floor(Math.random() * verbs.length)];
}

/**
 * Logs the killfeed occurrence of one pong'ing.
 * @param killer the player who kills
 * @param killed the player who is killed
 * @returns {HTMLElement} the element to be displayed
 */
function logEntryElement(killer, killed) {
    let newDiv = document.createElement("div");
    let logEnt = document.createElement("div");
    logEnt.className = "logEntry";
    if (killer !== "") {
        logEnt.innerHTML = killer + " <strong>" + getVerb() + "</strong> " + killed;
    } else {
        logEnt.innerHTML = killed + " <strong>disconnected</strong>"
    }
    newDiv.appendChild(logEnt);
    newDiv.appendChild(document.createElement("br"));
    return newDiv;
}

/**
 * Adds the
 * @param killer
 * @param killed
 */
function addToKillLog(killer, killed) {
    killLog.push([killer, killed, (new Date()).getTime()]);
    killLogElement.appendChild(logEntryElement(killer, killed));
}

/**
 * Params for fade-out of killfeed.
 * @type {number}
 */
const logTime = 5;
const fadeTime = 3;

/**
 * Draws killfeed properly, enables fade-out of old kill info.
 */
function drawKillLog() {
    const earliestLogTime = (new Date()).getTime() - logTime * 1000;
    const earliestFadeTime = (new Date()).getTime() - fadeTime * 1000;
    for (let i = logStartPointer; i < killLog.length; i++) {
        const log = killLog[i];
        if (log[2] < earliestLogTime) {
            logStartPointer = i + 1;
            // Was in the log, must be deleted
            killLogElement.removeChild(killLogElement.firstChild);
        } else if (log[2] < earliestFadeTime) {
            const opacity = 1 - (log[2] - earliestFadeTime) / (earliestLogTime - earliestFadeTime)
            killLogElement.children[i - logStartPointer].firstChild.style.opacity = opacity;
        }
    }
}

/**
 * Actually sets up and executes the pong game on document load.
 */
function executePong() {
    wsSetup();
    // Setting up the canvas.  Already has a width and height.
    canvas = $('#pong-canvas')[0];
    killLogElement = $("#killLog")[0];
    // Set up the canvas context.
    ctx = canvas.getContext("2d");
    ctx.fillStyle = "black";
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    //initialize 5 entities.
    up = false;
    down = false;
    playerPaddle = new Paddle(canvas.width / 2, canvas.height / 2, paddleWidth, paddleHeight, ctx);
    oppLeftPaddle = new Paddle(paddleWidth / 2, canvas.height / 2, paddleWidth, paddleHeight, ctx);
    oppRightPaddle = new Paddle(canvas.width - (paddleWidth / 2), canvas.height / 2, paddleWidth, paddleHeight, ctx);
    ballLeft = new Ball(ballSize, ctx, (canvas.width / 4) - (ballSize / 2), canvas.height / 2);
    ballRight = new Ball(ballSize, ctx, (3 * canvas.width / 4) - (ballSize / 2), canvas.height / 2);
    $(document).keydown(event => {
        checkPressed(event);
    });
    $(document).keyup(event => {
        checkUp(event);
    });
    inputHandle = setInterval(sendInput, 20);
    setInterval(drawKillLog, 20);
    $("#name").text(myId);
}

/**
 * Removes the waiting text when game starts.
 */
function rmWaitingText() {
    $("#status").text("");
}

/**
 * Executes on document load.
 */
$(document).ready(() => {
    executePong();
});
