package dev.mydogsed.sollexicalanalyzer.quotes.persist;

import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.Quote;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.QuoteAuthor;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionFactoryManager extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SessionFactoryManager.class);
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        log.debug("Building SessionFactory");
        // Prepare the database
        Configuration configuration = new Configuration();
        log.debug("Configuration object created");

        //configuration.configure("hibernate.properties"); // Load from resource file
        //log.debug("Properties file loaded");

        configuration.addAnnotatedClass(Quote.class);
        log.debug("Quote class added");

        configuration.addAnnotatedClass(QuoteAuthor.class);
        log.debug("Author class added");

        log.info("SessionFactory created, returning");
        return configuration.buildSessionFactory();

    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private static void disposeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        disposeSessionFactory();
    }
}
