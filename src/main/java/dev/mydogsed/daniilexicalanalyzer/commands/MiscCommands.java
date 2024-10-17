package dev.mydogsed.daniilexicalanalyzer.commands;

import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandDescription;
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

    // Counts the number of messages in a channel
    @SlashCommandExecutor("number")
    @SlashCommandDescription("Count the number of messages in this channel")
    public static void numberCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        List<Message> messages = getMessages(event.getChannel().asTextChannel());

        hook.editOriginal("The channel has " + messages.size() + " messages" ).queue();
    }

    // Uploads a file of the message history in that channel
    @SlashCommandExecutor("history")
    @SlashCommandDescription("Upload the channel history in a text file")
    public static void historyFileCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().setEphemeral(true).queue();
        List<Message> messages = getMessages(event.getChannel().asTextChannel());

        StringBuilder messageString = new StringBuilder();
        for (Message message : messages) {
            messageString.append(String.format("[%tc] %s: %s%n", message.getTimeCreated(), message.getAuthor().getEffectiveName(), message.getContentRaw()));
        }
        InputStream stream = new ByteArrayInputStream(messageString.toString().getBytes());
        FileUpload upload = FileUpload.fromData(stream, "channel_messages.txt");
        hook.editOriginalAttachments(upload).queue();
    }

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
}
