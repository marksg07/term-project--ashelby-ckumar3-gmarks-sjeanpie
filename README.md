# term-project

PONG 99 (Pong Battle Royale)


Member strengths and weaknesses:
Adam strengths: math & graph theory, aws & terraform automation experience, microserver construction experience, sqlite experience
Adam weaknesses: time management, front end

Gabriel strengths: coding, java, algorithms, backend, middle end, multiplayer games, socket.io
Gabriel weaknesses: frontend

Raj strengths: Physics mechanics, cs15/16, oop 
Raj weaknesses: UI, gui stuff

Sebastien strengths: cs15/16. Good at implementing pseudocode. Some AI / Machine Learning knowledge (cs1410) as well game theory concepts and algos. (have coded bots) 
Sebastien weaknesses: Shaky at graphics and gui implementations. 

Premise: Each player plays a game of pong in front and behind them. All the games create a logical circle of pong games such that each player is ALWAYS playing two games. When a player loses, the circle encloses. The game ends when only one player remains.

What problem is this idea attempting to solve?: In the growing popularity of battle royale type games, playing regular Pong is no longer as exciting. In addition, the base game of Pong requires very minimal skill. This new take on Pong seeks to create a new and exciting Pong experience in which you survive to be the last player standing, whilst the game gets progressively harder as the the number of players dwindles. Additionally, the game will feature some technical skill that makes the game experience more engaging and rewarding with the time put in. 

1 Basic Pong Engine
2 Online 2 Player Pong
 -> matchmaking
3 Online Last-2 Pong (circular board)
4 Online 3 Player Pong as a test for:
5 Online Modular-Number Pong
 -> matchmaking
-----------------------
ADDITIONAL IMPLEMENTATION:
Speed variation of ball over time & number of players decreasing
Game-invite urls
Bouncy Walls
Some ball control on bounce imposed on the pseudorandom bounce
Frame-precise input to get extra ball control
Powerups
Slow down opponents
Speed boost
Double paddle
Wide paddle
Dash (much faster than speed boost)
Extra bounce speed off your paddle
Extra ball control
Music & changing music
Save usernames & stats (sql database)
Save match data (sql database)
Teams of 2 
teammates could be placed back to back
Or teammates on different sides of map
Unlockables with more wins
