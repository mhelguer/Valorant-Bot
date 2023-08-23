package org.example;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import top.jfunc.json.impl.JSONObject;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.ArrayList;



public class ValAPI extends ListenerAdapter {
    static HashMap<Integer, ArrayList<String>> getMatchHistory(String name, String tag) throws LoginException, IOException {
        HashMap<Integer, ArrayList<String>> matches = new HashMap<Integer, ArrayList<String>>();
        // HTTP Request
        String nameNoSpace = name.replaceAll("\\s", "");
        URL url = new URL("https://api.henrikdev.xyz/valorant/v3/matches/na/" + nameNoSpace + "/"+tag);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:221.0) Gecko/20100101 Firefox/31.0"); // add this line to your code
        con.setRequestMethod("GET");


        // make sure player was found

        if(con.getResponseCode()==502){
            List<String> error = Arrays.asList("Bad gateway 502");
            matches.put(0, new ArrayList<>(error));
            return matches;
        }
        else if(con.getResponseCode()==503){
            List<String> error = Arrays.asList("Server error");
            matches.put(0, new ArrayList<>(error));
            return matches;
        }
        else if(con.getResponseCode()==403){
            List<String> error = Arrays.asList("Error 403 - Forbidden");
            matches.put(0, new ArrayList<>(error));
            return matches;
        }
        else if(con.getResponseCode()!=200){
            List<String> error = Arrays.asList("Username and tag not found");
            matches.put(0, new ArrayList<>(error));
            return matches;
        }


        // read response and convert to json
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        JSONObject jsonObj = new JSONObject(content.toString());
        Object dataObject = jsonObj.get("data");
        ArrayList data = new ArrayList();
        data = (ArrayList<String>) dataObject;

        // parse game data into jsonOjbect and get to array of player data
        Gson gson = new Gson();

        // iterate over array to output data from all 5 matches
        int match_number=0;
        for(Object match: data){
            JsonObject jsonObject = gson.toJsonTree(match).getAsJsonObject();

            JsonArray all_players = jsonObject.getAsJsonObject("players").getAsJsonArray("all_players");
            HashMap<String, Integer> names = getPlayerNames(all_players);

            int index = names.get(name);

            // put player's data into corresponding variables
            JsonObject metadata = jsonObject.getAsJsonObject("metadata");
            String map = metadata.get("map").getAsString();
            String mode = metadata.get("mode").getAsString();

            JsonObject player_data =all_players.get(index).getAsJsonObject();
            String team = player_data.get("team").getAsString();
            Integer levelInt = player_data.get("level").getAsInt();
            String level = levelInt.toString();
            String agent_name = player_data.get("character").getAsString();

            JsonObject stats = player_data.getAsJsonObject("stats");
            Integer scoreInt = stats.get("score").getAsInt();
            String score = scoreInt.toString();
            Integer killsInt = stats.get("kills").getAsInt();
            String kills = killsInt.toString();
            Integer deathsInt = stats.get("deaths").getAsInt();
            String deaths = deathsInt.toString();
            Integer assistsInt = stats.get("assists").getAsInt();
            String assists = assistsInt.toString();

            String player_team =  player_data.get("team").getAsString().toLowerCase();
            JsonObject match_data =jsonObject.getAsJsonObject("teams").getAsJsonObject(player_team);
            String match_result = match_data.get("has_won").getAsString();
            if(match_result.equals("true")){
                match_result="Won";
            }
            else{
                match_result="Lost";
            }
            Integer rounds_wonInt = match_data.get("rounds_won").getAsInt();
            String rounds_won = rounds_wonInt.toString();
            Integer rounds_lostInt = match_data.get("rounds_lost").getAsInt()       ;
            String rounds_lost = rounds_lostInt.toString();

        ArrayList<String> arr = new ArrayList<>();
        arr.add(name);
        arr.add(tag);
        arr.add(map);
        arr.add(mode);
        arr.add(team);
        arr.add(level);
        arr.add(agent_name);
        arr.add(score);
        arr.add(kills);
        arr.add(deaths);
        arr.add(assists);
        arr.add(rounds_won);
        arr.add(rounds_lost);
        arr.add(match_result);



//            List<? extends Serializable> arr = Arrays.asList(name,
//                    tag,
//                    map,
//                    mode,
//                    team,
//                    level,
//                    agent_name,
//                    score,
//                    kills,
//                    deaths,
//                    assists,
//                    rounds_won,
//                    rounds_lost,
//                    match_result);
            matches.put(match_number++, arr);
        }
        return matches;
    }

    public static  HashMap<String, Integer> getPlayerNames(JsonArray player_names){
        HashMap<String, Integer> names = new HashMap<>();

        for(int i =0; i<10;i++){
            names.put(player_names.get(i).getAsJsonObject().get("name").getAsString().replaceAll("\"\"", ""), i);
        }

        return names;

    }
    public static void main(String[] args) throws LoginException, IOException {

    }

}
