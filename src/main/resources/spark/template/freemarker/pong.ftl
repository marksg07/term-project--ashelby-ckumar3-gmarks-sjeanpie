<#assign content>
    <div class="wrapper">
        <div class="sidebar">
            <p>P O N G  F O L K S</p>
            <p id="status">P L A Y I N G</p>
        </div>
        <div class="pong-board">
            <div style="width: 800px; height: 300px;">
                <canvas id="pong-canvas" width="800" height="300">
                </canvas>
                <p id="leftCountdown"></p>
                <p id="rightCountdown"></p>
            </div>
            <p id="statusLeft">P L A Y I N G</p>
            <p id="statusRight">P L A Y I N G</p>

        </div>
    </div>
</#assign>
<#include "main.ftl">