package org.example;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.ValAPI;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;
public class DiscordBot extends ValAPI {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String bot_id = dotenv.get("BOT_ID").replaceAll("\"", "");
        bot_id=bot_id.replaceAll(" ", "");
        DiscordClient client = DiscordClientBuilder.create(bot_id).build();
        GatewayDiscordClient gateway = client.login().block();
        long applicationId = gateway.getRestClient().getApplicationId().block();
        Logger log = Loggers.getLogger(DiscordBot.class);

        // make bot reply "pong" when messaged "ping"
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            Message message = event.getMessage();
            if ("ping".equals(message.getContent())) {
                MessageChannel channel = message.getChannel().block();
                assert channel != null;
                channel.createMessage("Pong!").block();
            }

        });

        // random number command
        ApplicationCommandRequest randomCommand = ApplicationCommandRequest.builder()
                .name("random")
                .description("Send a random number")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("digits")
                        .description("Number of digits (1-20)")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .required(false).build())
                .build();
        gateway.getRestClient().getApplicationService().createGlobalApplicationCommand(applicationId, randomCommand).subscribe();

        // history command
        ApplicationCommandRequest historyCommand = ApplicationCommandRequest.builder()
                .name("history")
                .description("See match history")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("username")
                        .description("Your in-game username")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true).build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("tag")
                        .description("tagline(ex: #NA1)")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true).build())

                .build();
        gateway.getRestClient().getApplicationService().createGlobalApplicationCommand(applicationId, historyCommand).subscribe();


//        //DELETING COMMANDS
//        // get commands from discord as map
//        Map<String, ApplicationCommandData> discordCommands = gateway.getRestClient()
//                        .getApplicationService()
//                                .getGuildApplicationCommands(applicationId, guildId)
//                                        .collectMap(ApplicationCommandData::name)
//                                                .block();
//        long commandId = Long.parseLong(discordCommands.get("history").id().asString());

        //delete it
        //gateway.getRestClient().getApplicationService().deleteGuildApplicationCommand(applicationId, guildId, commandId).subscribe();

        gateway.on(ChatInputInteractionEvent.class, event-> {
            if (event.getCommandName().equals("history")) {
                try {
                    ;
                    return event.deferReply().then(getAPIResponse(event));
                } catch (LoginException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return Mono.empty();
        }).subscribe();

        gateway.onDisconnect().block();
    }

    private static Mono<Message> getAPIResponse(ChatInputInteractionEvent acid) throws LoginException, IOException {

        String username = String.valueOf(acid.getOption("username")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString));
        username = username.substring(9, username.indexOf("]"));

        String tag = String.valueOf(acid.getOption("tag")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString));
        tag = tag.substring(9, tag.indexOf("]"));
        String response = "";
        HashMap<Integer, ArrayList<String>> data = getMatchHistory(username, tag);

        // if player not found
        if(data.get(0).get(0).equals("Username not found")){
            response =  "Username not found";
            return acid.createFollowup(response);
        }
        // if valorant api not working
        else if(data.get(0).get(0).equals("Server error")){
            response = "Server error";
            return  acid.createFollowup(response);
        }
        else if(data.get(0).get(0).equals("Error 403 - Forbidden")){
            response = "Error 403 - Forbidden";
            return  acid.createFollowup(response);
        }
        else {
            String temp = "";


            for (int i = 0; i < 5; i++) {
                ArrayList<String> match_data = data.get(i);

                temp =
                        "Match " + (i + 1) + ":\n" +
                                "Username: " + match_data.get(0) + "\n" +
                                "Tag: " + match_data.get(1) + "\n" +
                                "Map: " + match_data.get(2) + "\n" +
                                "Mode: " + match_data.get(3) + "\n" +
                                "Team: " + match_data.get(4) + "\n" +
                                "Level: " + match_data.get(5) + "\n" +
                                "Agent: " + match_data.get(6) + "\n" +
                                "\nStats\n" +
                                "Score: " + match_data.get(7) + "\n" +
                                "Kills: " + match_data.get(8) + "\n" +
                                "Deaths: " + match_data.get(9) + "\n" +
                                "Assists: " + match_data.get(10) + "\n" +
                                "Rounds won: " + match_data.get(11) + "\n" +
                                "Rounds lost: " + match_data.get(12) + "\n" +
                                "Match result: " + match_data.get(13) + "\n-----------------------------------------------\n";

                response += temp;
            }
        }
        return acid.createFollowup(response);
    }
    private static String result(Random random, ApplicationCommandInteraction acid) {
        long digits = acid.getOption("digits")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .orElse(1L);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < Math.max(1, Math.min(20, digits)); i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }

}
