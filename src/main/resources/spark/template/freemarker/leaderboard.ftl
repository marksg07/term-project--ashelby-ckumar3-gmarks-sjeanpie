<#assign content>
    <div class="wrapper">
        <form action="/home" method="GET">
            <input type="submit" id="find-game" class="find_game" value="Go Home">
            </input>
        </form>
        <div class="pong-board">
            <div style="width: 800px; height: 300px;">
                <canvas id="pong-canvas" width="800" height="300" style="border:10px solid #164751;">

                </canvas>
                <div class="leaderboardtitle" class="alert-danger" id="leaderboarddiv">
                    LEADERBOARD
                </div>
                <div class="statstitleuser" id="user">
                    USER
                </div>
                <div class="statstitletotalgames" id="totalGames">
                    TOTAL GAMES
                </div>
                <div class="statstitleelo" id="elo">
                    ELO
                </div>
                <div class="statstitlewinrate" id="winRate">
                    WIN RATE
                </div>
            </div>
        </div>
    </div>
    <script src="js/jquery-2.1.1.js"></script>
    <script src="js/leaderboard.js"> </script>
</#assign>
<#include "main.ftl">
