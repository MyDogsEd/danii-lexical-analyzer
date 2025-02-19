package dev.mydogsed.sollexicalanalyzer.commands;

import dev.mydogsed.sollexicalanalyzer.DLAUtil;
import dev.mydogsed.sollexicalanalyzer.Main;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandExecutor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class QuotesCommands {

    public static EmbedBuilder quotesEmbed(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162));
    }

    public static EmbedBuilder randomQuotesEmbed(Message quote) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Random Quote")
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setFooter(quote.getAuthor().getEffectiveName(), quote.getAuthor().getAvatarUrl())
                .setTimestamp(quote.getTimeCreated());

        if (quote.getAttachments().isEmpty())
            return eb.addField(DLAUtil.getMessageContentRaw(quote), quote.getJumpUrl(), false);
        else
            return eb.setImage(DLAUtil.getMessageContentImage(quote).getUrl()).setDescription(quote.getJumpUrl());
    }

    @SlashCommandExecutor("numberquotes")
    @SlashCommandDescription("List the number of quotes in the #quotes-without-context channel")
    public static void numberQuotesCommand(SlashCommandInteractionEvent event){
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        hook.editOriginalEmbeds(
                quotesEmbed("Number of Quotes")
                        .setDescription("#quotes-without-context has " + quotesList().size() + " quotes archived." )
                        .build()
        ).queue();
    }

    @SlashCommandExecutor("randomquote")
    @SlashCommandDescription("Returns a random quote from the #quotes-without-context channel")
    public static void randomQuoteCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        hook.editOriginalEmbeds(randomQuotesEmbed(randomQuote()).build()).queue();
    }

    public static Message randomQuote(){
        return quotesList().get(new Random().nextInt(quotesList().size()));
    }

    private static List<Message> quotesList() {
        return Main.quotesCache.getMessages().stream().filter((Message m) -> {
            return m.getContentRaw().contains("\"") || // Message in quotes
                    m.getAttachments().size() == 1 || // Message is an image
                    m.getMessageSnapshots().size() == 1; // Forwarded Message
        }).toList();
    }
}
