<#assign content>
    <div class="wrapper">
        <form action="/home" method="GET">
            <input type="submit" id="find-game" class="find_game" value="Go Home">
            </input>
        </form>
        <div class="leaderboard" id="leaderboarddiv">
        </div>
    </div>
    <script src="js/jquery-2.1.1.js"></script>
    <script src="js/leaderboard.js"> </script>
</#assign>
<#include "main.ftl">
