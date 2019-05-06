<#assign content>
    <div class="wrapper">
        <div>
            <div class="pong-board horiz-centered">
                <div style="width: 800px; height: 300px;">
                    <canvas id="pong-canvas" width="830" height="300" style="border:10px solid #164751;">
                    </canvas>
                    <div class="flex-container">
                        <p id="leftCountdown"></p>
                        <p id="rightCountdown"></p>
                        <p id="midCountdown"></p>
                    </div>
                </div>
                <div class="nameFlex" style="margin-top: 30px;">
                    <div class="flexText" style="text-align: left;" id="leftName"></div>
                    <div class="flexText" style="text-align: center;" id="name"></div>
                    <div class="flexText" style="text-align: right;" id="rightName"></div>
                </div>
                <p id="status" class="horiz-centered" style="font-size: 48px; margin-top: 24px; bold: true;">Waiting for
                    players...</p> <br>
                <br>
                <form class="lowest  horiz-centered" action="/home" method="GET">
                    <input style="margin-top: 100px;" type="submit" class="lowest btn btn-outline-primary form-control"
                           id="find-game" class="find_game" value="Go Home">
                </form>
                <form style="margin-top: 30px;" hidden="true" id="new-game-form" class="more-lowest horiz-centered" action="/game" method="POST">
                    <input type="hidden" name="username" value="${username}">
                    <input type="hidden" id="userid" name="userid" value="${userid}">
                    <input style="margin-top: 100px;" type="submit" class="more-lowest btn btn-outline-primary form-control"
                           id="find-game" class="find_game" value="Find New Game">
                </form>
                <div id="killLog" class="floating"></div>
            </div>
        </div>
    </div>
    <script>
        const myId = "${username}";
        const userid = "${userid}";
    </script>
    <script src="js/jquery-2.1.1.js"></script>
    <script src="js/pong.js"></script>
    <script src="js/paddle.js"></script>
    <script src="js/ball.js"></script>
</#assign>
<#include "main.ftl">