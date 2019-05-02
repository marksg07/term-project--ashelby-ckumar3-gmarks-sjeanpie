<#assign content>
    <div class="wrapper">
        <form action="/home" method="GET">
            <input type="submit" id="find-game" class="find_game" value="Go Home">
            </input>
        </form>
        <div class="pong-board">
            <canvas id="pong-canvas" width="800" height="300" style="border:10px solid #164751;">
            </canvas>
            <p id="statusLeft">P L A Y I N G</p>
            <p id="statusRight">P L A Y I N G</p>
        </div>
    </div>
    <script src="js/jquery-2.1.1.js"></script>
    <script src="js/pong.js"> </script>
</#assign>
<#include "main.ftl">