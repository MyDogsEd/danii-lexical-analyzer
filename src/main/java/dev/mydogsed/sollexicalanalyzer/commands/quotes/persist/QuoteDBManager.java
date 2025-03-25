package dev.mydogsed.sollexicalanalyzer.commands.quotes.persist;

import dev.mydogsed.sollexicalanalyzer.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class QuoteDBManager extends ListenerAdapter {

    // ID For the quote channel
    public static final Long QUOTE_CHANNEL = 1233098767658520668L;

    // Fruity Factory server id
    public static final Long FRUITY_FACTORY = 1233092684198182943L;

    // Logging
    private static final Logger log = LoggerFactory.getLogger(QuoteDBManager.class);

    // Sync all messages in the quotes channel with the database
    public static void doMessageSync(JDA jda) {

        // Get the quotes TextChannel
        TextChannel quotesChannel = jda.getTextChannelById(QUOTE_CHANNEL);

        // If the channel is not found, error and return
        if (quotesChannel == null) {log.error("Quotes channel not found!"); return;}

        // Pull all the quotes from the quotes channel
        List<Message> quotes = pullQuotes(quotesChannel);

        // addOrUpdateQuote for each message
        for (Message message : quotes) {
            addOrUpdateQuote(message);
        }
    }

    public static void addOrUpdateQuote(Message message){
        if (!Util.isQuote(message)) {return;}

        try (Session session = SessionFactoryManager.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();

            // Try to get the quote author
            QuoteAuthor author = session.get(QuoteAuthor.class, message.getAuthor().getIdLong());

            // If the author exists
            if (author != null) {
                // ensure the author is up to date
                author.updateAuthor(message.getAuthor());
            }

            // If the author doesn't exist
            else {
                // Create a new author
                author = new QuoteAuthor(message.getAuthor());
            }

            // if the author already has this quote
            if (!author.getQuotes().isEmpty()
                    && author.containsQuote(message.getIdLong())){
                //update the quote content
                author.getQuote(message.getIdLong()).updateContent(message);
            }

            // If the author doesn't already have this quote
            else {
                // Create a new quote and add it to the author
                author.addQuote(new Quote(message));
            }

            // persist the author, which saves the quotes.
            session.persist(author);

            // commit the transaction to save to the database
            tx.commit();
        }

        // Catch any errors
        catch (Exception e) {
            log.error("Error while updating quote!", e);
        }

    }

    public static boolean quoteExists(Long id){
        try (Session session = SessionFactoryManager.getSessionFactory().openSession()){
            Quote quote = session.get(Quote.class, id);
            return quote != null;
        }
        catch (Exception e) {
            log.error("Error while checking if quote exists, returning false.", e);
            return false;
        }
    }

    public static Quote getQuote(Long id){
        try (Session session = SessionFactoryManager.getSessionFactory().openSession()){
            return session.get(Quote.class, id);
        } catch (Exception e){
            log.error("Error while getting quote, returning null.", e);
            return null;
        }
    }

    public static void removeQuote(Long id){
        try (Session session = SessionFactoryManager.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            Quote quote = session.get(Quote.class, id);
            if (quote == null) return;

            session.remove(quote);
            tx.commit();
        } catch (Exception e){
            log.error("Error while removing quote.", e);
        }
    }

    private static List<Message> pullQuotes(TextChannel textChannel) {
        // pull all the quotes, and sync them with the database

        // List to store the quotes
        List<Message> quotes = new LinkedList<>();

        // Make sure the channel exists
        if (textChannel == null) {log.error("Quotes channel not found"); return null;}

        // Create a MessageHistory object from the quotes channel
        MessageHistory messageHistory = textChannel.getHistory();


        // Get the past 100 messages
        List<Message> history = messageHistory.retrievePast(100).complete();
        while (true) {

            // For each message, if it's a quote, add it to the quotes list
            for (Message message : history) {
                if (Util.isQuote(message)) {
                    quotes.add(message);
                }
            }

            // If the history is less than 100,
            // we have reached the end of the channel, so break
            if (history.size() < 100) {
                break;
            }

            // Get the next 100 messages
            history = messageHistory.retrievePast(100).complete();
        }

        return quotes;
    }


}
