<html>
    <head>
        <script type="text/javascript">
            var c=3;
            var t;
            function timedCount()
            {
                document.getElementById('timer').innerHTML=c;
                c=c-1;
                if(c<=0)
                {
                    top.location='logoutforward.action';
                }
                t=setTimeout("timedCount()",1000);
            }
        </script>
    </head>
    <body onload="timedCount()">
        <div style="height:100px">
        </div>
        <div style="text-align:center">
            <p>You have updated your password Successfully.</p>
            <p>You will be logged out automatically after <div id="timer"></div> seconds.</p>
        </div>
    </body>
</html>
