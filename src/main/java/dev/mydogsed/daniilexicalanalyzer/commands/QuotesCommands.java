package dev.mydogsed.daniilexicalanalyzer.commands;

import dev.mydogsed.daniilexicalanalyzer.DLAUtil;
import dev.mydogsed.daniilexicalanalyzer.Main;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SlashCommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class QuotesCommands {

    static EmbedBuilder quotesEmbed(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setAuthor("danii-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setTimestamp(new Date().toInstant());
    }


    @SlashCommandExecutor("randomquote")
    @SlashCommandDescription("Returns a random quote from the #quotes-without-context channel")
    public static void randomQuoteCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        Message quote = randomQuote();
        EmbedBuilder eb = quotesEmbed("Random Quote")
                .addField(DLAUtil.getMessageContentRaw(quote), quote.getJumpUrl(), false);
        hook.editOriginalEmbeds(eb.build()).queue();

    }

    private static Message randomQuote(){
        List<Message> quotes = Main.quotesCache.getMessages().stream().filter((Message m) -> {
             return m.getContentRaw().contains("\"");
        }).toList();
        return quotes.get(new Random().nextInt(quotes.size()));
    }
}
