# sagrada-game
This is a mobile version of the board game Sagrada. Sagrada is a dice placement game where players assemble dice, represent the glass's colour and shade, and place them in their window - carefully following restrictions while adding their artistic flair. This mobile game is a Client-Server version of Sagrada with a console-based server(https://github.com/Onke/sagrada-server) created in Java.

## Running the game

Clone `sagrada-server` beside this repository, install JDK 17, then start the server:

```bash
cd ../sagrada-server
JAVA_HOME=/path/to/jdk-17 bash ../sagrada-game/gradlew -p . :server:run
```

Run the Android app and enter the IP address printed by the server. **Play vs Bots** supports one Android device and 1–3 server-controlled opponents. **LAN Multiplayer** waits for four Android players connected to the same server.

The home-screen **How to play** guide explains the rules and includes offline guided placement practice. The same guide is available from the `?` button during a match.

![sagrada_home](https://user-images.githubusercontent.com/79706797/118373456-2e939a80-b5b7-11eb-8292-93954ee144e2.JPG)


