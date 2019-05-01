<#assign content>
    <div class="wrapper">
        <form method="post" action="/login">
            Username:<br>
            <input type="text" name="name"></input><br>
            Password:<br>
            <input type="text" name="password"></input><br>
            <input type="submit" name="Log In"></input>
        </form>
        <div class="pong-board" style="text-align:center;">
            <canvas id="pong-canvas" width="800" height="300" style="border:10px solid #164751;">
            </canvas>
            <button id = "find-game" class="find_game">Find Game</button>
        </div>
    </div>
</#assign>
<#include "main.ftl">