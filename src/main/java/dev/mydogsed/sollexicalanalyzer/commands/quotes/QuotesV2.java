package dev.mydogsed.sollexicalanalyzer.commands.quotes;

import dev.mydogsed.sollexicalanalyzer.Main;
import dev.mydogsed.sollexicalanalyzer.Util;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommand;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandDescription;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommandName;
import dev.mydogsed.sollexicalanalyzer.commands.quotes.persist.Quote;
import dev.mydogsed.sollexicalanalyzer.commands.quotes.persist.QuoteAuthor;
import dev.mydogsed.sollexicalanalyzer.commands.quotes.persist.QuoteDBManager;
import dev.mydogsed.sollexicalanalyzer.commands.quotes.persist.SessionFactoryManager;
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
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class QuotesV2 implements SlashCommand {

    private static final Logger log = LoggerFactory.getLogger(QuotesV2.class);

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

        int size = QuoteDBManager.getQuotes().size();

        hook.editOriginalEmbeds(
                quotesEmbed("Number of Quotes")
                        .setDescription("#quotes-without-context has " + size + " quotes archived." )
                        .build()
        ).queue();
    }

    private void leaderboardCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        List<QuoteAuthor> authors = QuoteDBManager.getQuoteAuthors();

        int quotesSize = QuoteDBManager.getQuotes().size();

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
            quotes = QuoteDBManager.getQuoteAuthor(user.getIdLong()).getQuotes();
        }

        else {
            quotes = QuoteDBManager.getQuotes();
        }

        Quote randomQuote = quotes.get(new Random().nextInt(quotes.size()));

        if (new Random().nextInt(100) == 69) {
            hook.editOriginalAttachments(
                    FileUpload.fromData(
                            Objects.requireNonNull(QuotesV2.class.getResourceAsStream("/qiqi.jpg")),
                            "qiqi.jpg"
                    )
            ).queue();
        }

        else {
            EmbedBuilder eb = randomQuoteEmbed(randomQuote);
            hook.editOriginalEmbeds(eb.build()).queue();
        }
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
            QuoteDBManager.doMessageSync(event.getJDA());
            hook.editOriginal("Database updated").queue();
        }, "Quotes-Database-Sync-Thread");
        thread.start();
    }

    public static EmbedBuilder randomQuoteEmbed(Quote quote) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Random Quote")
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setFooter(quote.getAuthor().getUserName(), quote.getAuthor().getAvatarURL())
                .setTimestamp(quote.getTimeCreated());

        // is this a text quote?
        if (quote.isTextQuote()) {
            eb.addField(quote.getContent(), quote.getJumpURL(), false);
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
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setTimestamp(Instant.now());
    }
}
