# Evolution Battleship - Backend


## Description
This is a two-player game and each player has a 10x10 canvas and five ships to place, each of a different type:

- Destroyer (2 cells)
- Cruiser (3 cells)
- Submarine (3 cells)
- Battleship (4 cells)
- Carrier (5 cells)

A ship can be placed horizontally or vertically, but not diagonally. Once placed, the players can attack the other player's board by issuing coordinates one at a time. A player wins by being the first to sink all the ships on the opponent's board.


## Instructions
The following instructions show the output from the console-based entry as examples, the server-based entry will only show the game by **???** that will be used in the frontend project.

There are two distinct phases during the game, in this order:
* Placement - the player must place his own ships on the board.
* Attack - the player must attack the opponent's ships on the board.

### Ship Placement
During the ship placement phase, the board looks like:  

<pre>
    A   B   C   D   E   F   G   H   I   J  
 1  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪
 2  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚫  ⚪
 3  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚪  ⚫  ⚪
 4  ⚪  ⚫  ⚫   ⚪  ⚪  ⚫  ⚪   ⚪  ⚫  ⚪
 5  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚪  ⚪  ⚪
 6  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚪  ⚪  ⚪
 7  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚫  ⚪  ⚪
 8  ⚪  ⚫  ⚫   ⚫  ⚪  ⚪  ⚪   ⚫  ⚪  ⚪
 9  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚫  ⚪  ⚪
10  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚫  ⚪  ⚪
</pre>

where a `⚪` represents an empty cell and a `⚫` represents a placed ship.
<br>
Each player starts with an empty canvas of only `⚪`, as each ship is placed, the cells where those ships occupy become `⚫`.


### Ship Attack
During the ship attack phase, the board looks like below:
<pre>
    A   B   C   D   E   F   G   H   I   J
 1  ⚪  🔵  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪
 2  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  🔴  ⚪
 3  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪
 4  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪
 5  ⚪  ⚪  🔵   ⚪  ⚪  🔴  ⚪   ⚪  ⚪  ⚪
 6  ⚪  ⚪  ⚪   ⚪  ⚪  🔴  ⚪   ⚪  ⚪  ⚪
 7  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  🔵  ⚪
 8  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪
 9  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪
10  🔵  ⚪  🔵   ⚪  ⚪  ⚪  🔵   ⚪  ⚪  ⚪
</pre>

where `🔴` represents a hit and `🔵` represents a miss.

* To attack a coordinate: `X Y` e.g. `B 1`


### Ship Defend
In every attack, each player's own board is also displayed above his attacking board with the opponent's attacks and his own placed ships.
<br>
The board looks like below:
<pre>
    A   B   C   D   E   F   G   H   I   J  
 1  ⚪  🔵  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪
 2  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  🔴  ⚪
 3  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚪  ⚪  ⚪
 4  ⚪  ⚫  ⚫   ⚪  ⚪  ⚫  ⚪   ⚪  ⚪  ⚪
 5  ⚪  ⚪  🔵   ⚪  ⚪  🔴  ⚪   ⚪  ⚪  ⚪
 6  ⚪  ⚪  ⚪   ⚪  ⚪  🔴  ⚪   ⚪  ⚪  ⚪
 7  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚫  🔵  ⚪
 8  ⚪  ⚫  ⚫   ⚫  ⚪  ⚪  ⚪   ⚫  ⚪  ⚪
 9  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚫  ⚪  ⚪
10  🔵  ⚪  🔵   ⚪  ⚪  ⚪  🔵   ⚫  ⚪  ⚪
</pre>

This board is just representative once there is nothing the player is able to do to defend himself other than only watch his opponent attacking his board.


### Win or Lose
The game finishes when one player hits all the coordinates that have ships placed on the opponent's board making himself the winner and the opponent the loser.


## How to play?
The project is configured with **Scala 2.13.16** and uses **SBT** for build management.
<br> There are two possible ways to play the Evolution Battleship game on the backend, without frontend:

### Console-based:
Using the terminal, ...

* Run `sbt "runMain local.Main"`
  
* Place the 2-cells Destroyer, 3-cells Submarine, 3-cells Cruiser, 4-cells Battleship and 5-cells Carrier: `X1 Y1 X2 Y2`.
<br> (For example: `H 7 H 10` to place the 4-cell Battleship vertically, or `B 8 D 8` to place the 3-cells Submarine horizontally.)

* Attack any opponent's cell: `X Y`.
<br> (For example: `I 2` attacks the cell with those coordinates that can is a hit but could be a miss depending if there was not any opponent's placed ships.)

* The game ends when a player hits all the cells from the opponent's placed ships.


### Server-based:
Using Postman, ...

* Run `sbt "runMain server.ServerWebSockets"`

#### Creation Game

* **POST** `localhost:8000/createGame` - creates a new game room.

#### Join Room

* **GET** `localhost:8000/join/<roomId>/<player>`

    - `<roomId>` - game room's id provided in the creation of the game.
    - `<player>` - player's inputted name.

#### Place Ships

* 

#### Attack Ships

* 


// projeto backend...