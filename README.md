# Platformer

An editor and an engine that allows users to create and play their own Manic-Miner/Jet Set Willy style platform game (I started this for my children but some of it was nostalgia-driven).  The data created in the editor is saved and loaded using a mongdo db database.  A screen can be created with platforms and nasties, and the user can move and jump the player around it.  Using the editor the user can create their own player, objects and nasties and specify the boundaries of each one.

TODOS -   

1: todos in the code (for smaller issues) 

2: Need to make the speeds and distances that the sprites move and jump scale based on the screen size in the same way that the heights and widths do

3: Need to get rid of mongodb and have a file-based db instead so as it can all be easily distributed without users having to install mongodb and also having to somehow get hold of the data for the levels.  The data could be easily included in the distributable if the storage was file-based.

4: Include a set of instructions for distribution
