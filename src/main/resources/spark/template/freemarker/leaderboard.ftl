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
                <div class="leaderboardtitle">
                    <strong>LEADERBOARD</strong>

                </div>
                <table id="leaderboard">
                    <tr><th>Username</th>
                        <th>Total Games</th>
                        <th>ELO</th>
                        <th>Win Rate</th></tr>
                    <#if leaderboardData??>
                        <#list leaderboardData as entry>
                            <#if !entry??>
                                <tr><td>N/A</td>
                                    <td>N/A</td>
                                    <td>N/A</td>
                                    <td>N/A</td></tr>
                            <#else>
                            <tr><td>${entry.getUsr()}</td>
                                <td>${entry.getTotalGames()}</td>
                                <td>${entry.getElo()}</td>
                                <#if entry.getTotalGames() != 0>
                                    <td>${entry.getWins() * 100.0 / entry.getTotalGames()}%</td>
                                <#else>
                                    <td>0%</td>
                                </#if>
                            </tr>
                            </#if>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
    </div>
    <script src="js/jquery-2.1.1.js"></script>
    <script src="js/leaderboard.js"> </script>
</#assign>
<#include "main.ftl">
