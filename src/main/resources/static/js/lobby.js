let totalPlayers;
let players;

const generateLobby = event => {
    ctx.fillStyle = "black";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.font = "50px Futura, sans-serif";
    ctx.fillStyle = "white";
    ctx.textAlign = "center";
    ctx.fillText("Finding Players.....", canvas.width /2, canvas.height/2);
    executePong();
}
