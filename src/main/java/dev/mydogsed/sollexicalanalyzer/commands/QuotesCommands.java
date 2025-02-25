package dev.mydogsed.sollexicalanalyzer.commands;

import dev.mydogsed.sollexicalanalyzer.DLAUtil;
import dev.mydogsed.sollexicalanalyzer.Main;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandName;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class QuotesCommands {

    private static final Logger log = LoggerFactory.getLogger(QuotesCommands.class);

    public static EmbedBuilder quotesEmbed(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setTimestamp(Instant.now());
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

    @SlashCommandName("numberquotes")
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

    @SlashCommandName("leaderboard")
    @SlashCommandDescription("Display who has archived the most quotes")
    public static void leaderboardCommand(SlashCommandInteractionEvent event){
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        Map<String, Integer> map = new HashMap<>();
        List<Message> quotesList = quotesList();

        for (Message m : quotesList) {
            String user;
            //if (m.getAuthor().getIdLong() == 340161181526523907L)
                //user = "femboy josh";
            //else
            user = m.getAuthor().getName();
            map.put(user, map.getOrDefault(user, 0) + 1);
        }

        List<String> keys = new ArrayList<>(map.keySet().stream().toList());

        keys.sort(Comparator.comparing(map::get).reversed());

        // Only keep the top 7 people
        //keys = keys.subList(0, Math.min(keys.size(), 7));

        EmbedBuilder eb = quotesEmbed("Quotes Leaderboard");
        for(int i = 0; i < keys.size(); i++){
            String name = keys.get(i);
            int count = map.get(name);
            double percent = ((double)count / (double)quotesList().size()) * 100;
            eb.addField(
                    String.format("%d. %s",(i + 1), name), // "1. Tom"
                    String.format("%d quotes (%.1f%%)", count, percent), // "390 quotes (19.5%)"
                    false
            );
        }
        eb.setDescription("Total Quotes: "  + quotesList.size());
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandName("randomquote")
    @SlashCommandDescription("Returns a random quote from the #quotes-without-context channel")
    public static void randomQuoteCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        hook.editOriginalEmbeds(randomQuotesEmbed(randomQuote()).build()).queue();
    }

    @SlashCommandName("king")
    @SlashCommandDescription("All hail the king of the quotes channel")
    public static void kingCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        Map<String, Integer> map = new HashMap<>();
        List<Message> quotesList = quotesList();

        for (Message m : quotesList) {
            String user;
            //if (m.getAuthor().getIdLong() == 340161181526523907L)
            //user = "femboy josh";
            //else
            user = m.getAuthor().getName();
            map.put(user, map.getOrDefault(user, 0) + 1);
        }

        List<String> keys = new ArrayList<>(map.keySet().stream().toList());

        keys.sort(Comparator.comparing(map::get).reversed());

        // Only keep the top 1 people
        keys = keys.subList(0, Math.min(keys.size(), 1));
        EmbedBuilder eb = quotesEmbed("King of Quotes");
        eb.addField(keys.get(0), map.get(keys.get(0)) + " quotes archived", false);
        eb.setDescription("ALL HAIL THE KING OF THE QUOTES CHANNEL");

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandName("error")
    @SlashCommandDescription("oh no it broke")
    public static void errorCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        throw new RuntimeException("This command failed successfully.");
    }

    public static Message randomQuote(){
        return quotesList().get(new Random().nextInt(quotesList().size()));
    }

    private static List<Message> quotesList() {
        return Main.quotesCache.getMessages().stream().filter((Message m) -> {
            return m.getContentRaw().contains("\"") || // straight quotes
                    m.getContentRaw().contains("“") || // curly starting quote
                    m.getContentRaw().contains("”") || // curly ending quote
                    m.getContentRaw().contains(">") || // markdown quotes syntax
                    m.getAttachments().size() == 1 || // Message is an image
                    // is a forwarded message
                    m.getMessageReference() != null &&
                            m.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD;
        }).toList();
    }
}
