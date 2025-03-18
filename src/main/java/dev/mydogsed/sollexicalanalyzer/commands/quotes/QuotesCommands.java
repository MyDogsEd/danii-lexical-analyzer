package dev.mydogsed.sollexicalanalyzer.commands.quotes;

import dev.mydogsed.sollexicalanalyzer.DLAUtil;
import dev.mydogsed.sollexicalanalyzer.Main;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommand;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandName;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class QuotesCommands implements SlashCommand {

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

    public static void countCommand(SlashCommandInteractionEvent event){
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        hook.editOriginalEmbeds(
                quotesEmbed("Number of Quotes")
                        .setDescription("#quotes-without-context has " + quotesList().size() + " quotes archived." )
                        .build()
        ).queue();
    }

    // use the annotation for this command so that there is a global /leaderboard command
    @SlashCommandName("leaderboard")
    @SlashCommandDescription("Display who has archived the most quotes")
    public static void leaderboardCommand(SlashCommandInteractionEvent event){
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        Map<String, Integer> map = userQuotesMap();

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
        eb.setDescription("Total Quotes: "  + quotesList().size());
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @SlashCommandName("random")
    @SlashCommandDescription("Show a random quote from the quotes-without-context channel")
    public static void randomQuoteCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        int rand = new Random().nextInt(0, 25);
        if (rand == 0) {
            hook.editOriginalAttachments(
                    FileUpload.fromData(
                            Objects.requireNonNull(QuotesCommands.class.getResourceAsStream("/qiqi.jpg")),
                            "qiqi.jpg"
                    )
            ).queue();
        }
        else {
            hook.editOriginalEmbeds(randomQuotesEmbed(randomQuote()).build()).queue();
        }
    }

    public static void kingCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        // get the names and number of quotes
        Map<String, Integer> map = userQuotesMap();

        // assign the keys (names) to a new list
        List<String> keys = new ArrayList<>(map.keySet().stream().toList());

        // sort the names from 1st to last on quotes
        keys.sort(Comparator.comparing(map::get).reversed());

        // only keep the 1st person
        keys = keys.subList(0, Math.min(keys.size(), 1));
        EmbedBuilder eb = quotesEmbed("King of Quotes");
        eb.addField(keys.get(0), map.get(keys.get(0)) + " quotes archived", false);
        eb.setDescription("ALL HAIL THE KING OF THE QUOTES CHANNEL");

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    public static Message randomQuote(){
        return quotesList().get(new Random().nextInt(quotesList().size()));
    }

    private static List<Message> quotesList() {
        return Main.quotesCache.getMessages().stream().filter((Message m) -> {
            return m.getContentRaw().contains("\"") || // straight quotes
                    m.getContentRaw().contains("“") || // curly starting quote
                    m.getContentRaw().contains("”") || // curly ending quote
                    m.getContentRaw().startsWith(">") || // markdown quotes syntax
                    m.getAttachments().size() == 1 || // Message is an image
                    // is a forwarded message
                    m.getMessageReference() != null &&
                            m.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD ||
                    m.getAuthor().getIdLong() == 555955826880413696L; // epic rpg because its funnie
        }).toList();
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        switch(Objects.requireNonNull(event.getSubcommandName())) {
            case "count" -> countCommand(event);
            case "leaderboard" -> leaderboardCommand(event);
            case "random" -> randomQuoteCommand(event);
            case "king" -> kingCommand(event);
            case "stats" -> statsCommand(event);
        }
    }

    private void statsCommand(SlashCommandInteractionEvent event) {
        // Number attributed
        // first quote submitted
        // last quote submitted
        // leaderboard ranking
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        User user = (event.getInteraction().getOption("user") != null) ?
                event.getInteraction().getOption("user").getAsUser() :
                event.getUser();

        Map<String, Integer> map = userQuotesMap();

        Message firstQuote = quotesList()
                .stream()
                .filter(
                        (Message m) -> m.getAuthor().equals(user)
                )
                .sorted(
                        Comparator.comparing(ISnowflake::getTimeCreated)
                ).toList().get(0);



        Message lastQuote = quotesList()
                .stream()
                .filter(
                        (Message m) -> m.getAuthor().equals(user)
                )
                .sorted(
                        Comparator.comparing(ISnowflake::getTimeCreated).reversed()
                ).toList().get(0);

        EmbedBuilder eb = quotesEmbed(user.getEffectiveName())
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setDescription("Has archived " + map.getOrDefault(user.getName(), 0) + " quotes")
                .addField(
                        "first quote submitted",
                        firstQuote.getTimeCreated().format(DateTimeFormatter.ofPattern("d MMM uuuu"))
                                + "\n" + firstQuote.getJumpUrl(),
                        true
                )
                .addField(
                        "latest quote submitted",
                        lastQuote.getTimeCreated().format(DateTimeFormatter.ofPattern("d MMM uuuu"))
                                + "\n" + lastQuote.getJumpUrl(),
                        true
                )
                .addField(
                        "leaderboard ranking",
                        (leaderBoard().indexOf(user.getName()) + 1) + "/" + map.size(),
                        true
                );

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    @Override
    public SlashCommandData getData() {
        return Commands.slash("quotes", "quotes-without-context commands")
                .addSubcommands(
                        new SubcommandData("count", "Shows the number of quotes archived"),
                        new SubcommandData("leaderboard", "Rank members by how many quotes archived."),
                        new SubcommandData("random", "Show a random quote"),
                        new SubcommandData("king", "King of Quotes"),
                        new SubcommandData("stats", "List user quotes stats")
                                .addOption(
                                        OptionType.USER,
                                        "user",
                                        "The user whose stats should be shown (leave blank for your stats)",
                                        false
                                )
                );
    }

    public static Map<String, Integer> userQuotesMap() {
        Map<String, Integer> map = new HashMap<>();
        List<Message> quotesList = quotesList();
        for (Message m : quotesList) {
            String user;
            user = m.getAuthor().getName();
            map.put(user, map.getOrDefault(user, 0) + 1);
        }
        return map;
    }

    public static List<String> leaderBoard() {
        Map<String, Integer> map = userQuotesMap();
        List<String> list = new ArrayList<>(map.keySet());
        list.sort(Comparator.comparing(map::get).reversed());
        return list;
    }
}
