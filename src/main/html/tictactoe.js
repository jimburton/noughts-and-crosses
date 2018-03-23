/*
 * A Tic Tac Toe game in HTML/JavaScript/CSS.
 *
 * @author: Vasanth Krishnamoorthy
 */
var N_SIZE = 3,
    EMPTY = "&nbsp;",
    boxes = [],
    turn = "X",
    score,
    moves;

/*
 * Initializes the Tic Tac Toe board and starts the game.
 */
function initBoard() {
    var board = document.createElement('table');
    board.setAttribute("border", 1);
    board.setAttribute("cellspacing", 0);

    var identifier = 1;
    for (var i = 0; i < N_SIZE; i++) {
        var row = document.createElement('tr');
        board.appendChild(row);
        for (var j = 0; j < N_SIZE; j++) {
            var cell = document.createElement('td');
            cell.setAttribute('height', 120);
            cell.setAttribute('width', 120);
            cell.setAttribute('align', 'center');
            cell.setAttribute('valign', 'center');
            cell.classList.add('col' + j,'row' + i);
            if (i == j) {
                cell.classList.add('diagonal0');
            }
            if (j == N_SIZE - i - 1) {
                cell.classList.add('diagonal1');
            }
            cell.identifier = identifier;
            row.appendChild(cell);
            boxes.push(cell);
            identifier += identifier;
        }
    }

    document.getElementById("tictactoe").appendChild(board);
    disableBoard();
    startNewGame();
}

/*
 * New game
 */
function startNewGame() {
    score = {
        "X": 0,
        "O": 0
    };
    moves = 0;
    turn = "X";
    boxes.forEach(function (square) {
        square.innerHTML = EMPTY;
    });
    setupJoin();
}

/*
 * Check if a win or not
 */
function win(clicked) {
    // Get all cell classes
    var memberOf = clicked.className.split(/\s+/);
    for (var i = 0; i < memberOf.length; i++) {
        var testClass = '.' + memberOf[i];
        var items = contains('#tictactoe ' + testClass, turn);
        // winning condition: turn == N_SIZE
        if (items.length == N_SIZE) {
            return true;
        }
    }
    return false;
}

function contains(selector, text) {
    var elements = document.querySelectorAll(selector);
    return [].filter.call(elements, function(element){
        return RegExp(text).test(element.textContent);
    });
}

/*
 * Sets clicked square and also updates the turn.
 */
function set() {
    if (this.innerHTML !== EMPTY) {
        return;
    }
    setDirect(this.identifier);
    setTurnLabel();
    sendMove(this.identifier);
}

/*
 * Sets "clicked" square and updates the turn, can be called directly
 * rather than as an event handler.
 */
function setDirect(identifier) {
    var cell = getCellByIdentifier(identifier);
    cell.innerHTML = turn;
    moves += 1;
    score[turn] += identifier;
    if (win(cell)) {
        if(turn === side) {
            alert('You win!');
        } else {
            alert('You lose!');
        }
        send("LEAVE");
        inGame = false;
        startNewGame();
    } else if (moves === N_SIZE * N_SIZE) {
        alert("It's a draw!");
        inGame = false;
        startNewGame();
    } else {
        turn = turn === "X" ? "O" : "X";
    }
}

/*
 * Retrieve a cell from the table given an identifier
 */
function getCellByIdentifier(identifier) {
    var board = id('tictactoe').getElementsByTagName('table')[0];
    var rows = board.getElementsByTagName("tr");
    for(var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var arr = row.getElementsByTagName("td");
        for(var j = 0; j < arr.length; j++) {
            if (arr[j].identifier ==  identifier) {
                return arr[j];
            }
        }
    }
    return null;
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
    var board = id('tictactoe').getElementsByTagName('table')[0];
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

window.addEventListener("load", initBoard, false);
