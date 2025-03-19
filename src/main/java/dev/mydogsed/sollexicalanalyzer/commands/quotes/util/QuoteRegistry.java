package dev.mydogsed.sollexicalanalyzer.commands.quotes.util;

import dev.mydogsed.sollexicalanalyzer.DLAUtil;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.logging.Logger;

public class QuoteRegistry {

    private final Map<Long, Quote> map;

    public QuoteRegistry() {
        map = new HashMap<Long, Quote>();
    }

    public void addQuote(Message m){
        if (DLAUtil.isQuote(m)){
            map.put(m.getIdLong(), new Quote(m));
        } else {
            LoggerFactory.getLogger(QuoteRegistry.class).info("Not a quote: {}: {}", m.getAuthor().getEffectiveName(), m.getContentDisplay());
        }
    }

    public Quote getQuote(long id) {
        return map.get(id);
    }

    public Quote randomQuote() {
        return map.values().stream().toList().get(new Random().nextInt(map.size()));
    }

    public List<Quote> getAllQuotes() {
        return new ArrayList<>(map.values());
    }
}
