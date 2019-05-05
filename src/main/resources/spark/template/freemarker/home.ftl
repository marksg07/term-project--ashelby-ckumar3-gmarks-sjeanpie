<#assign content>
    <div class="wrapper">
        <div>
            <h2 class="grad-text horiz-centered top-header font-weight-bold">
                pongfolks
            </h2>
        </div>
        <div class="centered border rounded border-light">
            <div class="m-5" style="color:white">
                <h5>
                    ${response}
                </h5>
                <#if successful>
                    <!-- Shows if logged in. -->
                    <form method="post" style="color:white" action="/game">
                        <br>
                        <input type="submit" class="btn btn-outline-primary form-control" value="Find Game">
                        <!-- Store value for finding game. -->
                        <input type="hidden" name="username" value="${username}">
                        <input type="hidden" id="userid" name="userid" value="${userid}">
                        <br>
                    </form>
                    <form method="get" id="logout" style="color:white" action="/home">
                        <input type="submit" class="btn btn-outline-primary form-control" value="Logout">
                    </form>
                <#else>
                    <!-- Shows if not logged in. -->
                    <form method="post" class="login-push" style="color:white" action="/login">
                        Username:<br>
                        <input type="text" id="username" name="username" class="form-control">
                        Password:<br>
                        <input id="login-pass" type="password" name="password" class="form-control">
                        <input type="submit" name="Log In" value="Log In" class="btn btn-outline-primary form-control">
                        <br>
                        <br>
                        <div class="form-group">
                            New here? Why don't you
                            <input type="submit" name="Create Account" value="Create An Account"
                                   class="btn btn-outline-primary form-control">
                        </div>
                    </form>
                </#if>
                <br><br>
                <form action="/lb" method="GET" style="color:white">
                    Feeling competitive? Check out the
                    <input type="submit" name="leaderboard" value="Leaderboard"
                           class="btn btn-outline-primary form-control"><br>
                </form>
            </div>
        </div>
        <div class="bottom">
        </div>
    </div>
    <script src="js/jquery-2.1.1.js"></script>
    <script src="js/savecookie.js"></script>
    <script src="js/homepage.js"></script>
</#assign>
<#include "main.ftl">