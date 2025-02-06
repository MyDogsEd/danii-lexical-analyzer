package dev.mydogsed.daniilexicalanalyzer.commands.framework;

import dev.mydogsed.daniilexicalanalyzer.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MessageCache {

    private Map<Long, Message> cache;

    private final TextChannel activeChannel;

    // Create a new MessageCache
    public MessageCache(TextChannel textChannel) {
        // Set the active channel
        this.activeChannel = textChannel;
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
        for(Message message : messages){
            this.cache.put(message.getIdLong(), message);
        }

        // Register the listener
        Main.jda.addEventListener(new MessageCacheListener());
    }

    private void addMessage(Message message) {
        cache.put(message.getIdLong(), message);
    }

    private void updateMessage(Message message) {
        cache.put(message.getIdLong(), message);
    }

    private void deleteMessage(long messageId) {
        cache.remove(messageId);
    }

    // Listener Class to handle edited messages, etc
    class MessageCacheListener extends ListenerAdapter {

        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
            addMessage(event.getMessage());
        }

        public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
            updateMessage(event.getMessage());
        }

        public void onMessageDelete(@NotNull MessageDeleteEvent event) {
            deleteMessage(event.getMessageIdLong());
        }
    }
}
