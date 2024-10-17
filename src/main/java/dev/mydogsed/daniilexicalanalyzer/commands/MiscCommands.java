package dev.mydogsed.daniilexicalanalyzer.commands;

import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandExecutor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MiscCommands {

    public static List<Message> getMessages(TextChannel channel) {
        List<Message> messages = new ArrayList<>();
        MessageHistory messageHistory = channel.getHistory();
        while(true){
            var history = messageHistory.retrievePast(100).complete();
            messages.addAll(history);
            if (history.size() < 100){
                break;
            }
            messageHistory = channel.getHistoryAfter(messages.get(messages.size() - 1), 100).complete();
        }
        return messages;
    }

    // Counts the number of messages in a channel
    @SlashCommandExecutor("number")
    public static void numberCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        List<Message> messages = getMessages(event.getChannel().asTextChannel());

        hook.editOriginal("The channel has " + messages.size() + " messages" ).queue();
    }

    // Uploads a file of the message history in that channel
    @SlashCommandExecutor("historyfile")
    public static void historyFileCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().setEphemeral(true).queue();
        List<Message> messages = getMessages(event.getChannel().asTextChannel());

        String messageString = "";
        for (Message message : messages) {
            messageString += String.format("[%tc] %s: %s%n", message.getTimeCreated(), message.getAuthor().getEffectiveName(), message.getContentRaw());
        }
        InputStream stream = new ByteArrayInputStream(messageString.getBytes());
        FileUpload upload = FileUpload.fromData(stream, "channel_messages.txt");
        hook.editOriginalAttachments(upload).queue();
    }
}
