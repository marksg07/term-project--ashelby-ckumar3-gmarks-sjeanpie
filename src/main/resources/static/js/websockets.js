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
        if(!gameOver) {
            console.log("connection closed, restarting...");
            wsSetup();
        }
    }

    conn.onmessage = msg => {
        const data = JSON.parse(msg.data);
        // console.log(data);
        switch (data.type) {
            case MESSAGE_TYPE.REQUESTID:
                console.log('got requestid');
                // TODO Assign myId
                const idObj = {type: MESSAGE_TYPE.SENDID, payload: {id: myId, hash: hash}};
                conn.send(JSON.stringify(idObj));
                break;
            case MESSAGE_TYPE.GAMESTART:
                console.log('got gamestart');
                setGameReady(true);
                rmWaitingText();
                midSec.hide();
                break;
            case MESSAGE_TYPE.UPDATE:
                // console.log('got update');
                updateGame(data.payload.state);
                break;
            case MESSAGE_TYPE.PLAYERDEAD:
                console.log('got dead');
                onPlayerDead();
                break;
            case MESSAGE_TYPE.PLAYERWIN:
                console.log('got win epic victory royale #1');
                onPlayerWin();
                break;
            case MESSAGE_TYPE.BADID:
                console.log('got badid, redirect inc');
                window.location.pathname = "/home";
                break;
            case MESSAGE_TYPE.UPDATEUSERS:
                console.log('got user update!');
                setUsers(data.payload.left, data.payload.right);
                break;
            case MESSAGE_TYPE.KILLLOG:
                console.log('got kill log update!');
                console.log(data);
                addToKillLog(data.payload.killer, data.payload.killed);
                break;
            default:
                console.log('Unknown message type!', data.type);
                break;
        }
    };
    return conn;
}