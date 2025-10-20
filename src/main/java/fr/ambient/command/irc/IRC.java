package fr.ambient.command.irc;


import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.util.player.ChatUtil;
import lombok.SneakyThrows;

public class IRC {


    public  IRC(){
    }

    @SneakyThrows
    public void sendMessageTo(String topic, String message){


        if(!Ambient.getInstance().getConfig().getValue("irc-enabled").equals("true")){
            ChatUtil.display("You have irc disabled !");
            return;
        }

        //Ambient.getInstance().getBackend().sendEncrypted("irc:" + Ambient.getInstance().getToken() + ":" + message);
        JsonObject object = new JsonObject();
        object.addProperty("text", message);
        object.addProperty("id", "irc");

    }

}
