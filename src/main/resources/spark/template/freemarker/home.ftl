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
                    <form method="post" style="color:white" action="/game">
                        <br>
                        <button class="btn btn-outline-primary form-control">Find Game</button>
                        <br>
                    </form>
                <#else>
                <form method="post" style="color:white" action="/login">
                Username:<br>
                <input type="text" name="username" class="form-control">
                Password:<br>
                <input id="login-pass" type="password" name="password" class="form-control">
                <input type="submit" name="Log In" value="Log In" class="btn btn-outline-primary form-control">
                    <br>
                <br>
                <div class="form-group">
                    New here? Why don't you
                    <input type="submit" name="Create Account" value="Create An Account" class="btn btn-outline-primary form-control">
                </div>
                    </#if>
            </form>
            <br><br>
            <form action="/lb" method="GET" style="color:white">
                Feeling competitive? Check out the
                <input type="submit" name="leaderboard" value="Leaderboard" class="btn btn-outline-primary form-control"><br>
            </form>
            </div>
            </div>
    <div class="bottom">
        </div>
    </div>
</#assign>
<#include "main.ftl">