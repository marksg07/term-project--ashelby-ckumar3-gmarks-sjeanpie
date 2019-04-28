<#assign content>
    <div class="wrapper">
        <form>
            Username:<br>
            <input type="text" name="name"></input><br>
            Password:<br>
            <input type="text" name="password"></input><br>
            <input type="submit"></input>
        </form>
        <div class="pong-board">
            <canvas id="pong-canvas" width="800" height="300" style="border:10px solid #164751;">
            </canvas>
            <button id = "find-game" class="find_game">Find Game</button>
        </div>
    </div>
</#assign>
<#include "main.ftl">