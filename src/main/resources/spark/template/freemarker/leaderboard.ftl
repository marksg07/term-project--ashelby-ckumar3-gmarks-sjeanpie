<#assign content>
    <div class="wrapper">
        <h2 class="grad-text horiz-centered top-header font-weight-bold">
            LEADERBOARD
        </h2>
        <div class="centered" style="width: 800px; height: 300px;">
            <#--<canvas id="pong-canvas" width="800" height="300" style="border:10px solid #164751;">-->

            <#--</canvas>-->
            <!-- don't touch this leaderboard without changing the corresponding java! -->
            <table cellpadding="20" class="centered leaderboard border rounded border-light">
                <tr>
                    <th>Username</th>
                    <th>Total Games</th>
                    <th>ELO</th>
                    <th>Win Rate</th>
                </tr>
                <#if leaderboardData??>
                    <#list leaderboardData as entry>
                        <#if !entry??>
                            <tr>
                                <td>N/A</td>
                                <td>N/A</td>
                                <td>N/A</td>
                                <td>N/A</td>
                            </tr>
                        <#else>
                            <tr>
                                <td>${entry.getUsr()}</td>
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
            <form class="more-lowest horiz-centered" action="/home" method="GET">
                <input type="submit" id="find-game" class="btn btn-outline-primary find_game" value="Go Home">
            </form>
        </div>

    </div>
    <script src="js/jquery-2.1.1.js"></script>
    <script src="js/leaderboard.js"></script>
</#assign>
<#include "main.ftl">
