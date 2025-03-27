package dev.mydogsed.sollexicalanalyzer.quotes;

import dev.mydogsed.sollexicalanalyzer.framework.SlashCommand;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.QuotesDB;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.SessionFactoryManager;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.Quote;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.QuoteAuthor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Comparator;
import java.util.List;

import static dev.mydogsed.sollexicalanalyzer.quotes.QuotesUtil.quotesEmbed;

public class LeaderboardCommand implements SlashCommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("leaderboard", "Show the leaderboard")
                .addSubcommands(
                        new SubcommandData(
                                "quotes",
                                "Show the leaderboard by quotes"
                        ),
                        new SubcommandData(
                                "score",
                                "Show the leaderboard by score"
                        )
                );
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        switch (event.getSubcommandName()) {
            case "quotes" -> leaderboardQuotes(event);
            case "score" -> leaderboardScore(event);
        }

    }

    // Show the leaderboard by score
    private void leaderboardScore(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        List<QuoteAuthor> authors = QuotesDB.getQuoteAuthors();

        authors.sort(Comparator.comparingInt(QuoteAuthor::getTotalScore).reversed());

        EmbedBuilder eb = quotesEmbed("Score Leaderboard");
        int i = 0;
        for (QuoteAuthor author : authors) {
            eb.addField(
                    String.format("%d. %s",(i + 1), author.getUserName()), // "1. Tom"
                    String.format("Score: %d", author.getTotalScore()), // "390 quotes (19.5%)"
                    false
            );
            i++;
        }
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    private void leaderboardQuotes(SlashCommandInteractionEvent event) {
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
}
