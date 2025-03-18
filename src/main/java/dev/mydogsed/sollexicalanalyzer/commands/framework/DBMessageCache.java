package dev.mydogsed.sollexicalanalyzer.commands.framework;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DBMessageCache {

    private final Map<Long, Message> cache;

    public DBMessageCache(TextChannel textChannel, String dbFileName) {
        DB db = DBMaker
                .fileDB(dbFileName)
                .closeOnJvmShutdown()
                .make();


        HTreeMap<Long, Message> map = (HTreeMap<Long, Message>) db
                .hashMap("messages")
                .counterEnable()
                .keySerializer(Serializer.LONG)
                .createOrOpen();
    }

    private List<Message> getAllMessagesInChannel(TextChannel textChannel) {
        // Get all the messages in the channel
        List<Message> messages = new LinkedList<>();
        MessageHistory messageHistory = textChannel.getHistory();
        while(true){
            List<Message> history = messageHistory.retrievePast(100).complete();
            messages.addAll(history);
            if (history.size() < 100){
                break;
            }
            messageHistory = textChannel.getHistoryAfter(messages.get(messages.size() - 1), 100).complete();
        }
        return messages;
    }
}
