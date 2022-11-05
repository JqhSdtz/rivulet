<!DOCTYPE html>
<#--该页面用于确认sql更新，但目前不采用该方式-->
<html lang="zh-CN">
    <head>
        <style>
            body {
                background-color: #f0f0f0;
            }
            #sql-box {
                width: 95%;
                margin-left: 2.5%;
                margin-top: 10px;
                margin-bottom: 100px;
                background-color: white;
                box-shadow: 0 0 10px rgb(0 0 0 / 45%);
            }
            #sql-box pre {
                padding: 16px;
                word-break: break-all;
                white-space: pre-wrap;
            }
            #button-box {
                position: fixed;
                bottom: 20px;
                left: 0;
                right: 0;
                text-align: center;
            }
            #refresh-button, #confirm-button {
                height: 50px;
                width: 200px;
                font-size: large;
            }
            #confirm-button {
                margin-left: 50px;
            }
        </style>
        <title>确认执行SQL</title>
    </head>
    <body>
        <script>
            function refresh() {
                window.reloadIframe();
            }
            function confirm() {
                const confirmUpdateStructureSqlKey = prompt('请输入确认密钥');

            }
        </script>
        <div id="sql-box">
            <pre>${sql}</pre>
        </div>
        <div id="button-box">
            <button id="refresh-button" onclick="refresh()">刷新</button>
            <button id="confirm-button" onclick="confirm()">确认修改</button>
        </div>
    </body>
</html>
