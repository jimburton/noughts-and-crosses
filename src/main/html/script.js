//Establish the WebSocket connection and set up event handlers
var websocket;
var output;
var name = "...";
var inGame = false;

function init() {
    output = id("output");
    setupJoin();

    //Send message if "Send" is clicked
    id("send").addEventListener("click", function () {
        sendMessage(id("message").value);
    });

    //Send message if enter is pressed in the input field
    id("message").addEventListener("keypress", function (e) {
        if (e.keyCode === 13) { sendMessage(e.target.value); }
    });

    websocket = new WebSocket("ws://localhost:4567/game");
    websocket.onopen = function(evt) { onOpen(evt) };
    websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { handleMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
}

function onOpen(evt) {
    writeToScreen("CONNECTED");
}

function onClose(evt) {
    alert("WebSocket connection closed");
}

//Update the chat-panel, and the list of connected users
function handleMessage(msg) {
    var data = JSON.parse(msg.data);
    console.log(data);
    //console.log("Received: "+data.userlist);
    switch (data.msgType) {
        case "MOVE":
            move(data.move);
            break;
        case "NAME":
            break;
        case "NAME_ACK":
            name = data.userMessage;
            console.log("Name is now: "+name);
            setupLeave();
            break;
        case "LIST":
            console.log(data.userList);
            doUserList(data.userList, name);
            break;
    }
}

function doUserList(userList) {
    id("userlist").innerHTML = "";
    userList.forEach(function (user) {
        if(!(name === user)) {
            if (user.includes(" vs ")) {
                insert("userlist", "<li>" + user + "</a></li>");
            } else {
                var link = "<a href='#' onclick='return join(\""
                    + user + "\");' >" + user + "</a>";
                insert("userlist", "<li>" + link + "</a></li>");
            }
        }
    });
}

function join(p2) {
    send("JOIN", p2);
}

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "") {
        websocket.send(JSON.stringify(message));
        id("message").value = "";
    }
}

function move(cell) {
    //make the move
}

function setName(str) {
    name = str;
    insert("name_holder", name);
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}

function writeToScreen(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    output.appendChild(pre);
}

function sendName() {
    var theName = id("form_name_text").value;
    console.log(theName);
    send("NAME", theName);
}

function send(type, msg) {
    var data = {"msgType": type, "userMessage": msg};
    websocket.send(JSON.stringify(data));
}

function leave() {
    send("LEAVE", "");
    setupJoin();
    var node = id("userlist");
    node.innerHTML = "";
    while (node.firstChild) {
        node.removeChild(node.firstChild);
    }
}

function setupJoin() {
    id("form_name_text").style.display = 'block'
    id("form_name_submit").style.display = 'block'
    id("form_name_leave").style.display = 'none'
    id("name_holder").innerHTML = "";
}

function setupLeave() {
    id("form_name_text").style.display = 'none'
    id("form_name_submit").style.display = 'none'
    id("form_name_leave").style.display = 'block'
    id("name_holder").innerHTML = name;
}

window.addEventListener("load", init, false);