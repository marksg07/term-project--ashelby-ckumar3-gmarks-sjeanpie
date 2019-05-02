$(document).ready(() => {

    $.post("/stats", responseJSON => {
        // TODO: Parse the JSON response into a JavaScript object.
        const responseObject = JSON.parse(responseJSON);
        for(let i=0; i < responseObject.userData.length; i++){
            let newP = document.createElement('p');
            let t = document.createTextNode(responseObject.userData[i]);
            newP.appendChild(t);
            document.getElementById("leaderboarddiv").appendChild(newP);
        }
    });
});