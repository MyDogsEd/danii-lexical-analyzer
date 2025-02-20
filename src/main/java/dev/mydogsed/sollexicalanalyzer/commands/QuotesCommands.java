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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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

        // Is this a forwarded message?
        if (DLAUtil.isForwarded(quote)) {

            // does the snapshot have an attachment?
            if (!quote.getMessageSnapshots().get(0).getAttachments().isEmpty()) {
                return eb.setImage(quote.getMessageSnapshots().get(0).getAttachments().get(0).getUrl())
                        .setDescription(quote.getJumpUrl());
            }

            // No attachment, set the content to the forwarded message text
            else {
                return eb.addField(quote.getMessageSnapshots().get(0).getContentRaw(), quote.getJumpUrl(), false);
            }
        }

        // This is not a forwarded message
        else {

            // does the message have an attachment?
            if (!quote.getAttachments().isEmpty()) {
                return eb.setImage(quote.getAttachments().get(0).getUrl())
                        .setDescription(quote.getJumpUrl());
            }

            // No attachment, set the content to the message content
            else {
                return eb.addField(quote.getContentDisplay(), quote.getJumpUrl(), false);
            }
        }
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

    @SlashCommandExecutor("leaderboard")
    @SlashCommandDescription("Display who has archived the most quotes")
    public static void leaderboardCommand(SlashCommandInteractionEvent event){
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        Map<String, Integer> map = new HashMap<>();

        for (Message m : quotesList()) {
            String user;
            //if (m.getAuthor().getIdLong() == 340161181526523907L)
                //user = "femboy josh";
            //else
            user = m.getAuthor().getName();
            map.put(user, map.getOrDefault(user, 0) + 1);
        }

        List<String> keys = new ArrayList<>(map.keySet().stream().toList());

        keys.sort(Comparator.comparing(map::get).reversed());

        // Only keep the top 5 people
        keys = keys.subList(0, Math.min(keys.size(), 5));

        EmbedBuilder eb = quotesEmbed("Quotes Leaderboard");
        for(int i = 0; i < keys.size(); i++){
            eb.addField((i + 1) + ". " + keys.get(i), map.get(keys.get(i)) + " quotes", false);
        }
        hook.editOriginalEmbeds(eb.build()).queue();
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
