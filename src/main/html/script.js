/*
WebSocket client for playing Noughts and Crosses.
 */
var websocket;
var output;
var name = "...";
var player;
var inGame = false;

//Setup function called as the window.onload handler
function init() {
    output = id("name_holder");
    setupJoin();

    websocket = new WebSocket("ws://localhost:4567/game");
    websocket.onopen = function(evt) { onOpen(evt) };
    websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { handleMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
}

//Handler for websocket.onopen
function onOpen(evt) {
    //writeToScreen("CONNECTED");
}

//Handler for websocket.onclose
function onClose(evt) {
    alert("WebSocket connection closed");
}

//Main handler for incoming messages
function handleMessage(msg) {
    var data = JSON.parse(msg.data);
    console.log(data);
    //console.log("Received: "+data.userlist);
    switch (data.msgType) {
        case "MOVE":
            //received a move
            setDirect(data.userMessage);
            break;
        case "NAME":
            //name was rejected
            break;
        case "NAME_ACK":
            //name is accepted
            setName(data.userMessage);
            console.log("Name is now: "+name);
            setupLeave();
            break;
        case "LIST":
            //received list of users
            console.log(data.userList);
            doUserList(data.userList, name);
            break;
        case "PLAYER_1":
            //we started a game
            setNameAndPlayer("X", data.userMessage);
            enableBoard();
            break;
        case "PLAYER_2":
            //another player started a game with us
            setNameAndPlayer("O", data.userMessage);
            break;
        case "LEAVE":
            //was kicked out
            setupJoin();
            disableBoard();
            break;
    }
}

//enable the board for playing
function enableBoard() {
    setCellHandler(set);
}

//disable the board for playing
function disableBoard() {
    setCellHandler(function(){});
}

//assign an onlick handler to every cell in the table
function setCellHandler(handler) {
    var board = document.getElementById('tictactoe').getElementsByTagName('table')[0];
    var rows = board.getElementsByTagName("tr");
    for(var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var arr = row.getElementsByTagName("td");
        for(var j = 0; j < arr.length; j++) {
            (function(_j){
                //arr[_j].onclick = function() { alert(arr[_j].innerHTML); };
                arr[_j].onclick =  handler;
            })(j);
        }
    }
}

//Write a list of users to the document
function doUserList(userList) {
    id("userlist").innerHTML = "";
    userList.forEach(function (user) {
        var link;
        if(!(name === user)) {
            link = "<a href='#' onclick='return join(\""
                    + user + "\");' >" + user + "</a>";
        } else {
            link = "<strong>" + user + "</strong>";
        }
        insert("userlist", "<li>" + link + "</a></li>");
    });
    insert("userlist", "<li>Players online:</li>");
}

//Handler for selecting an opponent
function join(p2) {
    send("JOIN", p2);
}

//Handler for errors
function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

//handler for receiving a move from the server
function acceptMove(cell) {
    //make the move
    enableBoard();
}

//handler for receiving a move from the server
function sendMove(identifier) {
    send("MOVE", identifier);
    disableBoard();
}

//Handler for name being accepted by the server
function setName(str) {
    name = str;
    var label = "<strong>Playing as:</strong> {0}".formatUnicorn(name);
    insert("name_holder", label);
}

//Update name text when game begins
function setNameAndPlayer(str, other) {
    player = str;
    var label = "<strong>Playing as:</strong>  {0} [{1}] <strong>against</strong> {2}";
    label = label.formatUnicorn(name, player, other);
    id("name_holder").innerHTML = "";
    insert("name_holder", label);
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}

//Write to the output div
function writeToScreen(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    output.appendChild(pre);
}

//Send a possible name to the server
function sendName() {
    var theName = id("form_name_text").value;
    console.log(theName);
    send("NAME", theName);
}

//Send a JSON message with the given type
function send(type, msg) {
    var data = {"msgType": type, "userMessage": msg};
    websocket.send(JSON.stringify(data));
}

//Leave the game
function leave() {
    send("LEAVE", "");
    setupJoin();
    var node = id("userlist");
    node.innerHTML = "";
    while (node.firstChild) {
        node.removeChild(node.firstChild);
    }
    insert("userlist", "<li>Players online:</li>");
}

//Reset the name submission form, hiding the Quit button
function setupJoin() {
    id("form_name_text").style.display = 'block'
    id("form_name_submit").style.display = 'block'
    id("form_name_leave").style.display = 'none'
    id("name_holder").innerHTML = "";
}

//Reset the name submission form, hiding the Name submission fields
function setupLeave() {
    id("form_name_text").style.display = 'none'
    id("form_name_submit").style.display = 'none'
    id("form_name_leave").style.display = 'block'
}

//printf style function
String.prototype.formatUnicorn = String.prototype.formatUnicorn ||
    function () {
        "use strict";
        var str = this.toString();
        if (arguments.length) {
            var t = typeof arguments[0];
            var key;
            var args = ("string" === t || "number" === t) ?
                Array.prototype.slice.call(arguments)
                : arguments[0];

            for (key in args) {
                str = str.replace(new RegExp("\\{" + key + "\\}", "gi"), args[key]);
            }
        }

        return str;
    };

//Call the inti function when the page has loaded all resources
window.addEventListener("load", init, false);