package dev.mydogsed.sollexicalanalyzer.commands.quotes.util;

import dev.mydogsed.sollexicalanalyzer.Util;
import dev.mydogsed.sollexicalanalyzer.Main;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommand;
import dev.mydogsed.sollexicalanalyzer.commands.quotes.QuotesCommands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QuotesV2 implements SlashCommand {

    private QuoteRegistry registry;

    @Override
    public SlashCommandData getData() {
        return Commands.slash("admin", "x")
                .addSubcommands(
                        new SubcommandData("random", "x"),
                        new SubcommandData("leaderboard", "x"),
                        new SubcommandData("migrate", "x")
                );
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (event.getUser().getIdLong() != 335802802335121408L){
            event.reply("https://c.tenor.com/pFeLhIX6b5cAAAAd/tenor.gif");
        }

        switch(Objects.requireNonNull(event.getSubcommandName())){
            case "random" -> randomCommand(event);
            case "leaderboard" -> leaderboardCommand(event);
            case "migrate" -> migrateCommand(event);
        }
    }

    private void randomCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        // qiqi joke
        int rand = new Random().nextInt(0, 25);
        if (rand == 0) {
            hook.editOriginalAttachments(
                    FileUpload.fromData(
                            Objects.requireNonNull(QuotesCommands.class.getResourceAsStream("/qiqi.jpg")),
                            "qiqi.jpg"
                    )
            ).queue();
            return;
        }

        // send embed
        Quote quote = registry.randomQuote();

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Random Quote")
                .setAuthor("sol-lexical-analyzer BETA TEST", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setFooter(quote.getAuthor().getEffectiveName(), quote.getAuthor().getAvatarUrl())
                .setTimestamp(quote.getTimeCreated());

        if (quote.isTextQuote()) {
            eb.addField(quote.getContentDisplay(), quote.getJumpURL(), false);
        } else {
            eb.setImage(quote.getImageURL()).setDescription(quote.getJumpURL());
        }

        hook.editOriginalEmbeds(eb.build()).queue();

    }

    private void leaderboardCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        Map<String, Integer> userQuotesMap = new HashMap<>();
        List<Quote> quotesList = registry.getAllQuotes();
        int iter = 1;
        for(Quote quote : quotesList){
            String user;
            user = quote.getAuthor().getName();
            userQuotesMap.put(user, userQuotesMap.getOrDefault(user, 0) + 1);
            System.out.println("calculating quote " + iter + "/" + quotesList.size() + "\r");
            iter++;
        }

        List<String> usernames = new ArrayList<>(userQuotesMap.keySet().stream().toList());
        usernames.sort(Comparator.comparing(userQuotesMap::get).reversed());

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Quotes Leaderboard")
                .setAuthor("sol-lexical-analyzer BETA TEST", "https://mydogsed.dev", Main.jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setTimestamp(Instant.now());

        for(int i = 0; i < usernames.size(); i++){
            String name = usernames.get(i);
            int count = userQuotesMap.get(name);
            double percent = ((double)count / (double)quotesList.size()) * 100;
            eb.addField(
                    String.format("%d. %s",(i + 1), name), // "1. Tom"
                    String.format("%d quotes (%.1f%%)", count, percent), // "390 quotes (19.5%)"
                    false
            );
        }
        eb.setDescription("Total Quotes: "  + quotesList.size());
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    private void migrateCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        hook.editOriginal("Migrating. This might take a while.").queue();
        registry = new QuoteRegistry();

        List<Message> messages = Main.quotesCache.getMessages().stream().filter(Util::isQuote).toList();

        AtomicInteger i = new AtomicInteger(1);
        for(Message m : messages){
            if (Util.isQuote(m)){
                registry.addQuote(m);
            }
            System.out.print("Migrating " + i + "/" + messages.size() + "\r");
            i.getAndIncrement();
        }
        hook.editOriginal("Migrating finished!").queue();
    }


}
