package dev.mydogsed.sollexicalanalyzer.misc;

import dev.mydogsed.sollexicalanalyzer.Util;
import dev.mydogsed.sollexicalanalyzer.framework.CommandRegistry;
import dev.mydogsed.sollexicalanalyzer.framework.SlashCommandDescription;
import dev.mydogsed.sollexicalanalyzer.framework.SlashCommandName;
import dev.mydogsed.sollexicalanalyzer.quotes.QuotesCommands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;


public class MiscCommands {

    // Uploads a file of the message history in that channel
    @SlashCommandName("history")
    @SlashCommandDescription("Upload the channel history in a text file")
    public static void historyFileCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().setEphemeral(true).queue();
        hook.editOriginal("Getting messages. This could take a long time! (Up to 2 minutes)").queue();
        List<Message> messages = getMessages(event.getChannel().asTextChannel());

        StringBuilder messageString = new StringBuilder();
        for (Message message : messages) {
            messageString.append(String.format("[%tc] %s: %s%n", message.getTimeCreated(), message.getAuthor().getEffectiveName(), Util.getMessageContentRaw(message)));
        }
        InputStream stream = new ByteArrayInputStream(messageString.toString().getBytes());
        FileUpload upload = FileUpload.fromData(stream, "channel_messages.txt");
        hook.editOriginalAttachments(upload).queue();
    }

    @SlashCommandName("help")
    @SlashCommandDescription("Show the help dialog")
    public static void helpCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        EmbedBuilder eb = QuotesCommands.quotesEmbed("Help");
        CommandRegistry registry = CommandRegistry.getInstance();
        Set<String> commandNames = registry.getCommandNames();
        for(String commandName : commandNames) {
            eb.addField("/" + commandName, registry.getExecutor(commandName).getData().getDescription(), true);
        }
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandName("hello")
    @SlashCommandDescription("Hello!")
    public static void helloCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        LinkedList<String> scout = new LinkedList<>();
        InputStream is = Objects.requireNonNull(MiscCommands.class.getResourceAsStream("/scout.txt"));
        Scanner scan = new Scanner(is);
        while (scan.hasNextLine()) {
            scout.add(scan.nextLine());
        }

        int rand = new Random().nextInt(scout.size());

        hook.editOriginal(scout.get(rand)).queue();

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
