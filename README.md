# Evolution Battleship Game


## Description
This is a two-player game and each player has a 10x10 canvas and five ships to place, each of a different type:

- Cruiser (1 hole)
- Destroyer (2 holes)
- Submarine (3 holes)
- Battleship (4 holes)
- Carrier (5 holes)

A ship can be placed horizontally or vertically, but not diagonally.
In addition, no ships can be in adjacent cells on the board.

Once placed, the players can attack the other player's board by issuing coordinates. A player wins by being the first to take down all the ships on the other player's board.


## Commands

There are two distinct phases during the game, in this order:
* Placement - the player must place his own ships on the board
* Attack - the player must attack the other player's ships on the board

### Ship Placement
During the ship placement phase, the board looks like:  

<pre>
    A   B   C   D   E   F   G   H   I   J  
 1  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪
 2  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚪  ⚫  ⚪
 3  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚪  ⚪  ⚪
 4  ⚪  ⚫  ⚫   ⚪  ⚪  ⚫  ⚪   ⚪  ⚪  ⚪
 5  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚪  ⚪  ⚪
 6  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚪  ⚪  ⚪
 7  ⚪  ⚪  ⚪   ⚪  ⚪  ⚫  ⚪   ⚫  ⚪  ⚪
 8  ⚪  ⚫  ⚫   ⚫  ⚪  ⚪  ⚪   ⚫  ⚪  ⚪
 9  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚫  ⚪  ⚪
10  ⚪  ⚪  ⚪   ⚪  ⚪  ⚪  ⚪   ⚫  ⚪  ⚪
</pre>

where a `⚪` represents an empty cell and a `⚫` represents a placed ship.
<br>
Each player starts with an empty canvas only of `⚪`, as each ship is placed, the cells where those ships occupy become `⚫`.


* To place your 1-cell submarine: `X1 Y1 X2 Y2` where X1=X2 and Y1=Y2. For example: `A 1 A 1`  
    

* To place your 2-cell destroyer, 3 holes Submarine and 4 holes Battleship: `X1 Y1 X2 Y2`.
* For example: `H 7 H 10` to place the 4-cell battleship vertically.


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

where ...
// it is just representitive once there is nothing a player is able to do to defend himself.


## Win
The game finishes when one player hits all the coordinates that have ships placed on the other player's board.