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
    conn = new WebSocket("ws://" + window.location.hostname + "/gamesocket");
    conn.onerror = err => {
        console.log('Connection error:', err);
    };

    conn.onmessage = msg => {
        const data = JSON.parse(msg.data);
        switch (data.type) {
            default:
                console.log('Unknown message type!', data.type);
                break;
            case MESSAGE_TYPE.REQUESTID:
                // TODO Assign myId
                const idObj = {type: MESSAGE_TYPE.SENDID, payload: {id: myId, hash: 0}};
                conn.send(JSON.stringify(idObj));
                break;
            case MESSAGE_TYPE.GAMESTART:
                break;
            case MESSAGE_TYPE.UPDATE:

                // TODO Update the relevant row or add a new row to the scores table
                const id = data.payload.id;
                const score = data.payload.score;
                if(id === myId) {
                    $("#score").text(score);
                    if(score > prevScore)
                        $("#message").text("Good job! You guessed the word!");
                    else
                        $("#message").text("Wrong word, try again.");
                    prevScore = score;
                    return;
                }
                const $table = $("#otherScores");
                const matchingRows = $("#otherScores tr").filter(function() {
                    console.log($(this).find("td")[0]);
                    return $($(this).find("td")[0]).text() === "" + id;
                });
                if(matchingRows.length > 0) {
                    $($(matchingRows[0]).find("td")[1]).text(score);
                } else {
                    const newRow = '<tr><td>' + id + '</td><td>' + score + '</td></tr>';
                    $table.append(newRow);
                }
                break;
        }
    };
}