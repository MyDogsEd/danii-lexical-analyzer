package dev.mydogsed.daniilexicalanalyzer.commands;

import dev.mydogsed.daniilexicalanalyzer.Main;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandExecutor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class QuotesCommands {

    @SlashCommandExecutor("randomquote")
    @SlashCommandDescription("Returns a random quote from the #quotes-without-context channel")
    public static void randomQuoteCommand(SlashCommandInteractionEvent event) {

    }

    private static Message randomQuote(){
        List<Message> quotes = Main.quotesCache.getMessages().stream().filter((Message m) -> {
             return m.getContentRaw().contains("\"");
        }).toList();

    }
}
