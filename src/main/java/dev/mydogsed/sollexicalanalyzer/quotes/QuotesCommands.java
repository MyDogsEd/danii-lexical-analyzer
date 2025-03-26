package dev.mydogsed.sollexicalanalyzer.quotes;

import dev.mydogsed.sollexicalanalyzer.Main;
import dev.mydogsed.sollexicalanalyzer.framework.SlashCommand;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.Quote;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.QuoteAuthor;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.QuotesDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static dev.mydogsed.sollexicalanalyzer.Main.jda;

public class QuotesCommands implements SlashCommand {

    private static final Logger log = LoggerFactory.getLogger(QuotesCommands.class);
    private static final Emoji up = jda.getEmojiById(1233196810793783356L);
    private static final Emoji down = jda.getEmojiById(1313221080659394660L);

    @Override
    public SlashCommandData getData() {
        return Commands.slash("quotes", "admin database commands")
                .addSubcommands(
                        new SubcommandData("count", "Shows the number of quotes archived"),
                        new SubcommandData("leaderboard", "Rank members by how many quotes archived."),
                        new SubcommandData("random", "Show a random quote")
                                .addOption(
                                        OptionType.USER,
                                        "user",
                                        "show quotes from a specific user",
                                        false
                                ),
                        new SubcommandData("sync", "Sync the database to the quotes channel")
                );
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "count" -> countCommand(event);
            case "leaderboard" -> leaderboardCommand(event);
            case "sync" -> migrateCommand(event);
            case "random" -> randomCommand(event);
        }
    }

    private void countCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        int size = QuotesDB.getQuotes().size();

        hook.editOriginalEmbeds(
                quotesEmbed("Number of Quotes")
                        .setDescription("#quotes-without-context has " + size + " quotes archived." )
                        .build()
        ).queue();
    }

    private void leaderboardCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        List<QuoteAuthor> authors = QuotesDB.getQuoteAuthors();

        int quotesSize = QuotesDB.getQuotes().size();

        authors.sort(
                Comparator.comparingInt((QuoteAuthor o) -> o.getQuotes().size()).reversed()
        );

        EmbedBuilder eb = quotesEmbed("Quotes Leaderboard");
        int i = 0;
        for(QuoteAuthor qa : authors){
            int count = qa.getQuotes().size();
            double percent = ((double)count / (double)quotesSize) * 100;
            eb.addField(
                    String.format("%d. %s",(i + 1), qa.getUserName()), // "1. Tom"
                    String.format("%d quotes (%.1f%%)", count, percent), // "390 quotes (19.5%)"
                    false
            );
            i++;
        }
        eb.setDescription("Total Quotes: " + quotesSize);
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    private void randomCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        List<Quote> quotes;

        if (event.getOption("user") != null) {
            User user = event.getOption("user").getAsUser();
            quotes = QuotesDB.getQuoteAuthor(user.getIdLong()).getQuotes();
        }

        else {
            quotes = QuotesDB.getQuotes();
        }

        Quote randomQuote = quotes.get(new Random().nextInt(quotes.size()));

        if (new Random().nextInt(100) == 69) {
            hook.editOriginalAttachments(
                    FileUpload.fromData(
                            Objects.requireNonNull(QuotesCommands.class.getResourceAsStream("/qiqi.jpg")),
                            "qiqi.jpg"
                    )
            ).queue();
            return;
        }

        EmbedBuilder eb = randomQuoteEmbed(randomQuote);
        hook.editOriginalEmbeds(eb.build()).setActionRow(
                Button.of(ButtonStyle.PRIMARY, "upvote", up),
                Button.of(ButtonStyle.DANGER, "downvote", down)
        ).queue(m -> handleButtonInteraction(m, event, randomQuote));

    }

private void handleButtonInteraction(Message message, SlashCommandInteractionEvent event, Quote quote) {
        long userId = event.getUser().getIdLong();
        long messageId = message.getIdLong();

        ListenerAdapter buttonListener = new ListenerAdapter() {

            final Set<User> interactedUsers = new HashSet<>();

            @Override
            public void onButtonInteraction(ButtonInteractionEvent event) {
                super.onButtonInteraction(event);

                if (event.getUser().isBot()) {return;}
                if (event.getMessage().getIdLong() != messageId) {return;}
                if (interactedUsers.contains(event.getUser())) {
                    event.getInteraction().reply("You can only react to a rolled quote once!").setEphemeral(true).queue();
                    return;
                }

                interactedUsers.add(event.getUser());

                // Acknowledge the event
                InteractionHook hook = event.getHook();
                event.getInteraction().deferEdit().queue();

                // Get the component ID of the clicked button
                String componentID = event.getComponentId();

                if (componentID.equals("upvote")) {
                    quote.voteUp();
                    QuotesDB.persistOrMergeQuote(quote);
                }
                else {
                    quote.voteDown();
                    QuotesDB.persistOrMergeQuote(quote);
                }

                // Edit the embed to update the score of the quote
                hook.editOriginalEmbeds(randomQuoteEmbed(quote).build()).queue();
            }
        };

        event.getJDA().addEventListener(buttonListener);

        new Timer().schedule(new TimerTask() {
            public void run() {

                // Disable the actionRows
                var ar = message.getActionRows().get(0);
                event.getHook().editOriginalComponents(ar.asDisabled());

                // Unregister the event listener
                event.getJDA().removeEventListener(buttonListener);
            }
        }, 600_000); // 600,000 ms is 10 minutes
    }

    private void migrateCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        if (event.getUser().getIdLong() != 335802802335121408L){
            hook.editOriginal("https://c.tenor.com/pFeLhIX6b5cAAAAd/tenor.gif").queue();
            return;
        }

        hook.editOriginal("Updating database. This could take up to 3 minutes.").queue();

        Thread thread = new Thread(() -> {
            QuotesDB.doMessageSync(event.getJDA());
            hook.editOriginal("Database updated").queue();
        }, "Quotes-Database-Sync-Thread");
        thread.start();
    }

    public static EmbedBuilder randomQuoteEmbed(Quote quote) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Random Quote")
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setFooter(quote.getAuthor().getUserName(), quote.getAuthor().getAvatarURL())
                .setTimestamp(quote.getTimeCreated());

        // is this a text quote?
        if (quote.isTextQuote()) {
            eb.addField(quote.getContent(), quote.getJumpURL() + "\nScore: " + quote.getScore(), false);
        }

        else {
            eb.setImage(quote.getImageURL());
            eb.setDescription(quote.getJumpURL());
        }

        return eb;
    }

    public static EmbedBuilder quotesEmbed(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setTimestamp(Instant.now());
    }
}
