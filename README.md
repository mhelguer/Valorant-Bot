# About
This Discord Bot uses Java, discord4j, and Dotenv to search for and display the match data for the entered user's previous 5 Valorant matches.
The API used is [henrikdev's valorant api](https://docs.henrikdev.xyz/valorant.html).

# Getting Started
You will need to first put the bot's ID into the .env file located at the root of the project, and use an invite link to put the bot in
your server. Next, run the code in IntelliJ and you will then be able to use the `/history` command in the server that the bot is in.
You will need 2 parameters for the command to work: the Valorant user's username and tag (example: NA1). The bot will then display
data from the user's previous 5 valorant matches, regardless of which game mode they were in.
