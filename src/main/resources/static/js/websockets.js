let conn;

const MESSAGE_TYPE = {
    REQUESTID: 0,
    SENDID: 1,
    GAMESTART: 2,
    INPUT: 3,
    UPDATE: 4,
    PLAYERDEAD: 5,
    PLAYERWIN: 6,
    BADID: 7,
    UPDATEUSERS: 8,
    KILLLOG: 9
};

function wsSetup() {
    /*Packet descriptions:
 * REQUESTID: Server->client, contains nothing. Prompted by client connect.
 * SENDID: Client->server, sends ID and password hash. Prompted by request ID packet.
 * GAMESTART: Server->client, contains nothing. Prompted by
 *  server when matchmaking is ready.
 * INPUT: Client->server, contains value of input and ID. Prompted by client periodically.
 * UPDATE: Server->client, contains ID of server and game data. Prompted by client input (XXX).
 * PLAYERDEAD: me dead
 */
    conn = new WebSocket("wss://" + window.location.hostname + ":" + window.location.port + "/gamesocket");
    conn.onerror = err => {
        console.log('Connection error:', err);
    };

    conn.onclose = evt => {
        if (!gameOver) {
            console.log("connection closed, restarting...");
            wsSetup();
        }
    };

    conn.onmessage = msg => {
        const data = JSON.parse(msg.data);
        switch (data.type) {
            case MESSAGE_TYPE.REQUESTID:
                // Send my ID back to the server
                const idObj = {type: MESSAGE_TYPE.SENDID, payload: {id: myId, userid: userid}};
                conn.send(JSON.stringify(idObj));
                break;
            case MESSAGE_TYPE.GAMESTART:
                rmWaitingText();
                midSec.hide();
                break;
            case MESSAGE_TYPE.UPDATE:
                updateGame(data.payload.state);
                break;
            case MESSAGE_TYPE.PLAYERDEAD:
                onPlayerDead();
                break;
            case MESSAGE_TYPE.PLAYERWIN:
                onPlayerWin();
                break;
            case MESSAGE_TYPE.BADID:
                window.location.pathname = "/home";
                break;
            case MESSAGE_TYPE.UPDATEUSERS:
                setUsers(data.payload.left, data.payload.right);
                break;
            case MESSAGE_TYPE.KILLLOG:
                addToKillLog(data.payload.killer, data.payload.killed);
                break;
            default:
                console.log("bad packet");
                break;
        }
    };
    return conn;
}