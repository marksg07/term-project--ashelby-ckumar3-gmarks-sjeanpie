<#assign content>
    <div class="wrapper">
    	<p>${response} </p>
        <form method="post" action="/login">
            Username:<br>
            <textarea name="username" placeholder="Enter username here"></textarea><br>
            Password:<br>
            <textarea name="password"></textarea><br>
            <input type="submit" name="Log In" value="Log In"></input>
            <input type="submit" name="Create Account" value="Create Account"></input>
        </form>
        <div class="centered">
            <form action="/game" method="GET">
                <input type="submit" id="find-game" class="find_game" value="Find Game">
                </input>
            </form>
    </div>
</#assign>
<#include "main.ftl">