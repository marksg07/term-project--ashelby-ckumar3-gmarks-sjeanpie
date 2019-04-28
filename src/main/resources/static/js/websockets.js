let conn;

const MESSAGE_TYPE = {
    REQUESTID: 0,
    SENDID: 1,
    GAMESTART: 2,
    INPUT: 3,
    UPDATE: 4
};

function wsSetup() {
    /*Packet descriptions:
 * REQUESTID: Server->client, contains nothing. Prompted by client connect.
 * SENDID: Client->server, sends ID and password hash. Prompted by request ID packet.
 * GAMESTART: Server->client, contains nothing. Prompted by
 *  server when matchmaking is ready.
 * INPUT: Client->server, contains value of input and ID. Prompted by client periodically.
 * UPDATE: Server->client, contains ID of server and game data. Prompted by client input (XXX).
 */
    conn = new WebSocket("ws://" + window.location.hostname + ":" + window.location.port + "/gamesocket");
    conn.onerror = err => {
        console.log('Connection error:', err);
    };

    conn.onmessage = msg => {
        const data = JSON.parse(msg.data);
        console.log(data);
        switch (data.type) {
            default:
                console.log('Unknown message type!', data.type);
                break;
            case MESSAGE_TYPE.REQUESTID:
                console.log('got requestid');
                // TODO Assign myId
                const idObj = {type: MESSAGE_TYPE.SENDID, payload: {id: myId, hash: 0}};
                conn.send(JSON.stringify(idObj));
                break;
            case MESSAGE_TYPE.GAMESTART:
                console.log('got gamestart');
                setGameReady(true);
                break;
            case MESSAGE_TYPE.UPDATE:
                console.log('got update');
                updateGame(data.payload.state);
                break;
        }
    };
    return conn;
}