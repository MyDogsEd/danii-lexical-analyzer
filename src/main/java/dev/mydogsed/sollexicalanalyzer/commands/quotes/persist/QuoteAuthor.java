package dev.mydogsed.sollexicalanalyzer.commands.quotes.persist;

import jakarta.persistence.*;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

@Entity
public class QuoteAuthor {

    // Discord id for this author
    @Id
    private Long id;
    private String userName;
    private String avatarURL;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quote> quotes = new ArrayList<>();


    // for hibernate, we have to have a default constructor
    public QuoteAuthor() {}

    public QuoteAuthor(User user) {
        this.id = user.getIdLong();
        this.updateAuthor(user);
    }

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public List<Quote> getQuotes() {
        return quotes;
    }

    public void addQuote(Quote quote) {
        quotes.add(quote);
        quote.setAuthor(this);
    }

    public void removeQuote(Quote quote) {
        quotes.remove(quote);
        quote.setAuthor(null);
    }

    public void updateAuthor(User user) {
        this.userName = user.getName();
        this.avatarURL = user.getAvatarUrl();
    }

    public boolean containsQuote(Long id){
        for(Quote quote : quotes){
            if (quote.getMessageID().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public Quote getQuote(Long id) {
        for(Quote quote : quotes){
            if (quote.getMessageID().equals(id)) {
                return quote;
            }
        }
        return null;
    }
}
