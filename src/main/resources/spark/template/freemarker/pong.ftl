<#assign content>
    <div class="wrapper">
        <div>
        <div class="pong-board horiz-centered">
            <div style="width: 800px; height: 300px;">
                <canvas id="pong-canvas" width="830" height="300" style="border:10px solid #164751;">
                </canvas>
                <p id="leftCountdown"></p>
                <p id="rightCountdown"></p>
            </div>
            <div class="nameFlex">
                <div style="align-self: flex-start; text-align: left;" id="leftName"></div>
                <div style="align-self: center; text-align: center;" id="name"></div>
                <div style="align-self: flex-end; text-align: right;" id="rightName"></div>
            </div>
            <p id="status" class="horiz-centered" style="font-size: 48px; margin-top: 24px; bold: true;">Waiting for players...</p>

            <form class="lowest horiz-centered" action="/home" method="GET">
                <input style="margin-top: 100px;" type="submit" class="lowest btn btn-outline-primary form-control" id="find-game" class="find_game" value="Go Home">
            </form>
        </div>
        </div>
    </div>
    <script>
        const myId = "${username}";
        const hash = "${hash}";
    </script>
    <script src="js/jquery-2.1.1.js"></script>
    <script src="js/pong.js"> </script>
</#assign>
<#include "main.ftl">