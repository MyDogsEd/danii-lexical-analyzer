package dev.mydogsed.sollexicalanalyzer.commands.quotes;

import dev.mydogsed.sollexicalanalyzer.Main;
import dev.mydogsed.sollexicalanalyzer.Util;
import dev.mydogsed.sollexicalanalyzer.commands.framework.SlashCommand;
import dev.mydogsed.sollexicalanalyzer.commands.quotes.persist.Quote;
import dev.mydogsed.sollexicalanalyzer.commands.quotes.persist.QuoteAuthor;
import dev.mydogsed.sollexicalanalyzer.commands.quotes.persist.SessionFactoryManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class QuotesV2 implements SlashCommand {

    private static final Logger log = LoggerFactory.getLogger(QuotesV2.class);

    @Override
    public SlashCommandData getData() {
        return Commands.slash("db", "admin database commands")
                .addSubcommands(
                        new SubcommandData(
                                "migrate",
                                "Migrate the database from the current MessageCache system"
                        )
                );
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (event.getUser().getIdLong() != 335802802335121408L){
            event.reply("https://c.tenor.com/pFeLhIX6b5cAAAAd/tenor.gif");
        }
        if (Objects.requireNonNull(event.getSubcommandName()).equals("migrate")) {
            migrateCommand(event);
        }
    }

    private void migrateCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        try (Session session = SessionFactoryManager.getSessionFactory().openSession()) {

            // Start a transaction
            Transaction t = session.beginTransaction();

            // Loop through all messages in the quotesCache
            for(Message m : Main.quotesCache.getMessages()){

                // If the message is not a quote, skip it
                if (!Util.isQuote(m)) continue;

                // Try to get the author from the author table
                QuoteAuthor author = session.get(QuoteAuthor.class, m.getAuthor().getIdLong());

                // if the author was found, add the quote to them
                if (author != null) {
                    author.addQuote(new Quote(m));
                }

                // if the author was not found, create a new author and set the quote to them
                else {
                    author = new QuoteAuthor(m.getAuthor());
                    author.addQuote(new Quote(m));
                }

                // persist the updated author object
                session.persist(author);
            }

            t.commit();
        }

        catch (Exception e) {
            log.error(e.getMessage());
        }

        hook.editOriginal("Database updated!").queue();
    }
}
