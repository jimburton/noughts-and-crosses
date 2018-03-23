/*
WebSocket client for playing Noughts and Crosses.
 */
var websocket;
var output;
var name = "...";
var inGame = false;

//Setup function called as the window.onload handler
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

//Handler for websocket.onopen
function onOpen(evt) {
    writeToScreen("CONNECTED");
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

//Write a list of users to the document
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

//Handler for selecting an opponent
function join(p2) {
    send("JOIN", p2);
}

//Handler for errors
function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

//handler for clicking on the board
function move(cell) {
    //make the move
}

//Handler for name being accepted by the server
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
    id("name_holder").innerHTML = name;
}

//Call the inti function when the page has loaded all resources
window.addEventListener("load", init, false);