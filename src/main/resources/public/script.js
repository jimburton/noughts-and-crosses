/*
WebSocket client for playing Noughts and Crosses.
 */
var websocket;
var side;//"X" or "0"
var name;
var opponentName;
var inGame;

//Setup function called as the window.onload handler
function init() {
    setupJoin();
    websocket = new WebSocket("ws://localhost:4567/game");
    websocket.onopen = function(evt) { onOpen(evt) };
    websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { handleMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
}

//Handler for websocket.onopen
function onOpen(evt) {}

//Handler for websocket.onclose
function onClose(evt) {
    alert("WebSocket connection closed");
}

//Main handler for incoming messages
function handleMessage(msg) {
    var data = JSON.parse(msg.data);
    console.log(data);
    switch (data.msgType) {
        case "MOVE":
            //received a move
            setDirect(data.userMessage);
            setTurnLabel();
            enableBoard();
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
            inGame = true;
            setNameAndPlayer("X", data.userMessage, data.userList);
            enableBoard();
            toggleChat(true);
            break;
        case "PLAYER_2":
            //another player started a game with us
            inGame = true;
            setNameAndPlayer("O", data.userMessage, data.userList);
            toggleChat(true);
            break;
        case "LEAVE":
            //was kicked out
            toggleChat(false);
            break;
        case "CHAT":
            id("chat_area").innerHTML += "\n"+data.userMessage;
            break;
    }
}

//clear the userlist
function resetUserList() {
    var node = id("userlist");
    node.innerHTML = "";
    while (node.firstChild) {
        node.removeChild(node.firstChild);
    }
    insert("userlist", "<li>Offline</li>");
}

//Write a list of users to the document
function doUserList(userList) {
    id("userlist").innerHTML = "";
    userList.forEach(function (user) {
        var link;
        if(!(name === user)) {
            if (inGame) {
                link = user;
            } else {
                link = "<a href='#' onclick='return join(\""
                    + user + "\");' >" + user + "</a>";
            }
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
    id("name_holder").innerHTML = '<span class="error">ERROR:</span> ' + evt.data;
}

//Send a move to the server
function sendMove(identifier) {
    send("MOVE", identifier);
    disableBoard();
    setTurnLabel();
}

//Send a possible name to the server
function sendName() {
    var theName = id("form_name_text").value;
    console.log(theName);
    send("NAME", theName);
}

//Handler for name being accepted by the server
function setName(str) {
    name = str;
    var label = "<strong>Online as:</strong> {0}".formatUnicorn(name);
    insert("name_holder", label);
}

//Update name text when game begins
function setNameAndPlayer(str, other, userList) {
    side = str;
    opponentName = other;
    var label = "<strong>Playing as:</strong>  {0} [{1}] <strong>against</strong> {2}";
    label = label.formatUnicorn(name, side, other);
    id("name_holder").innerHTML = label;
    setTurnLabel();
    doUserList(userList);
}

function setTurnLabel() {
    if(!inGame) {
        id("turn").innerHTML = "";
    } else {
        var turnLabel = "It is <strong>{0}</strong> turn"
        var turnInner = (turn === side ? "your" : opponentName + "'s");
        turnLabel = turnLabel.formatUnicorn(turnInner);
        id("turn").innerHTML = turnLabel;
    }
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}

//Send a JSON message with the given type
function send(type, msg) {
    var data = {"msgType": type, "userMessage": msg};
    websocket.send(JSON.stringify(data));
}

//Leave the game
function leave() {
    inGame = false;
    send("LEAVE", "");
    setupJoin();
    resetUserList();
    startNewGame();
}

//Reset the name submission form, hiding the Quit button
function setupJoin() {
    id("form_name_text").style.display = 'block'
    id("form_name_submit").style.display = 'block'
    id("form_name_leave").style.display = 'none'
    id("name_holder").innerHTML = "";
    id("turn").innerHTML = "";
    resetUserList();
}

//Reset the name submission form, hiding the Name submission fields
function setupLeave() {
    id("form_name_text").style.display = 'none'
    id("form_name_submit").style.display = 'none'
    id("form_name_leave").style.display = 'block'
    id("name_holder").innerHTML = "";
    id("turn").innerHTML = "";
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

/////////////////////
// Chat functionality
/////////////////////

//Send a message to the opponent player
function chat() {
    var msg = id("form_chat_text").innerHTML;
    send("CHAT", msg);
    id("form_chat_text").innerHTML = "";
}

// Toggle the availability of the chat form
function toggleChat(b) {
    id("form_chat").disabled = !b;
    id("form_chat_text").disabled = !b;
    id("form_chat_submit").disabled = !b;
}

//////////////////////
// Handle window events
//////////////////////

//Call the init function when the page has loaded all resources
window.addEventListener("load", init, false);

//warn the user when leaving a game
window.addEventListener("beforeunload", function (e) {
    if(inGame) {
        var confirmationMessage = 'Are you sure you want to leave the game?';
        (e || window.event).returnValue = confirmationMessage; //Gecko + IE
        return confirmationMessage; //Gecko + Webkit, Safari, Chrome etc.
    } else {
        return true;
    }
});