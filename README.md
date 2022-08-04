# ConsoleGame
A Text Based, Turn-Based, Strategy Game to be played in console. Feel free to fix any retardation
in my code (meaning bugs, stupid/spaghetti code or useless shit).

## The Game
### Goal of the Game
This Game is a Territory Control game. In this particular case there are `n` "Capture Points",
each of these can be unowned or owned by one of the two players. Each player starts with one
owned capture point, these are generated to be as far away from each other as possible.
As soon as one player has captured every point on the map and finishes his turn, he wins.

### Controls
The Game is controlled through different commands, these are as follows:

* move      <origin-coords>   <destination-coords> Move a troop
* attack    <attacker-coords> <defender-coords>    Attack another troop
* capture   <attacker-coords> <cp-coords>          Attempt to capture a point
* deploy    <deploy-coords>   <troop-name>         Attempt to deploy specified Troop at specified (owned) CP
* troop     <troop-coords>                         Get Information about a troop
* primary   <troop-coords>                         Execute the primary action of the troop at given coordinates
* secondary <troop-coords>                         Execute the secondary action of the troop at given coordinates

### How to Play
The Game is exclusively multiplayer and requires a Server to run.