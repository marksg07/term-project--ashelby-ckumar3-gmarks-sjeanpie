<#assign content>
    <div class="wrapper">
        <form>
            Username:<br>
            <input type="text" name="name"></input><br>
            Password:<br>
            <input type="text" name="password"></input><br>
            <input type="submit"></input>
        </form>
        <div class="centered">
            <form action="/game" method="GET">
                <input type="submit" id="find-game" class="find_game" value="Find Game">
                </input>
            </form>
        </div>
    </div>
</#assign>
<#include "main.ftl">