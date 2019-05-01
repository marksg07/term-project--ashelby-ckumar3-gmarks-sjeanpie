<!DOCTYPE html>

<head>
    <meta charset="utf-8">
    <title>${title}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- In real-world webapps, css is usually minified and
         concatenated. Here, separate normalize from our code, and
         avoid minification for clarity. -->


    <link rel="stylesheet" href="css/pong.css">
</head>
<body>
${content}
<!-- Again, we're serving up the unminified source for clarity. -->
<script src="js/jquery-2.1.1.js"></script>
<script src="js/websockets.js"> </script>
<script src="js/homepage.js"> </script>
<#--<script src="js/pong.js"> </script>-->
<script src="js/lobby.js"> </script>
<script src="js/paddle.js"> </script>
<script src="js/ball.js"> </script>

</body>
<!-- See http://html5boilerplate.com/ for a good place to start
     dealing with real world issues like old browsers.  -->
</html>
