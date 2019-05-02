$(document).ready(() => {

    $.post("/stats", responseJSON => {
        // TODO: Parse the JSON response into a JavaScript object.
        const responseObject = JSON.parse(responseJSON);
        for(let i=0; i < responseObject.userData.length; i++){
            let newP = document.createElement('p');
            let t = document.createTextNode(responseObject.userData[i]);
            newP.appendChild(t);
            console.log(i);
            if (i % 4 === 0) {
                document.getElementById("user").appendChild(newP);
            } else if (i % 4 === 1) {
                document.getElementById("totalGames").appendChild(newP);
            } else if (i % 4 === 2) {
                document.getElementById("elo").appendChild(newP);
            } else {
                document.getElementById("winRate").appendChild(newP);
            }
        }
    });
});