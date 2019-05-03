<#assign content>
    <div class="wrapper">
        <form action="/home" method="GET">
            <input type="submit" id="find-game" class="find_game" value="Go Home">
        </form>
        <div class="pong-board">
            <div style="width: 800px; height: 300px;">
                <canvas id="pong-canvas" width="800" height="300" style="border:10px solid #164751;">
                </canvas>
                <p id="leftCountdown"></p>
                <p id="rightCountdown"></p>
            </div>
            <p id="status" style="font-size: 48px; margin-top: 24px; bold: true;">Waiting for players...</p>
        </div>
    </div>
    <script src="js/jquery-2.1.1.js"></script>
    <script src="js/pong.js"> </script>
</#assign>
<#include "main.ftl">