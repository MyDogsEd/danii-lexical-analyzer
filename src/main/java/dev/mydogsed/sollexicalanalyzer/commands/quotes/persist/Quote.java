package dev.mydogsed.sollexicalanalyzer.commands.quotes.persist;

import dev.mydogsed.sollexicalanalyzer.Util;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

@Entity
public class Quote {

    // the same as the message's ID on discord
    @Id
    private Long id;

    // Message content
    private String content;

    // If the quote is an image
    private String imageURL;

    // QuoteAuthor
    @ManyToOne
    @JoinColumn(name = "author_id")
    private QuoteAuthor author;

    // The time that the quote was submitted
    private OffsetDateTime timeCreated;

    // The jump url to the message on discord
    private String jumpURL;

    // Whether this is a text or image quote
    private boolean isTextQuote;

    public Quote() {}

    public Quote(Message message) {

        this.id = message.getIdLong();
        this.timeCreated = message.getTimeCreated();
        this.jumpURL = message.getJumpUrl();

        this.updateContent(message);

    }

    public boolean isTextQuote() {
        return isTextQuote;
    }

    public String getContent() {
        return content;
    }


    public String getImageURL() {
        return imageURL;
    }

    public Long getMessageID() {
        return id;
    }

    public OffsetDateTime getTimeCreated() {
        return timeCreated;
    }

    public String getJumpURL() {
        return jumpURL;
    }

    public QuoteAuthor getAuthor() {
        return author;
    }

    public void setAuthor(QuoteAuthor author) {
        this.author = author;
    }

    public void updateContent(Message message) {
        if (Util.isTextQuote(message)) {
            this.content = message.getContentRaw();
            this.isTextQuote = true;
        }

        else if (Util.isForwardedTextQuote(message)) {
            this.content = message.getMessageSnapshots().get(0).getContentRaw();
            this.isTextQuote = true;
        }

        else if (Util.isImageQuote(message)) {
            this.imageURL = message.getAttachments().get(0).getUrl();
            this.isTextQuote = false;
        }

        else if (Util.isForwardedImageQuote(message)) {
            this.imageURL = message.getMessageSnapshots().get(0).getAttachments().get(0).getUrl();
            this.isTextQuote = false;
        }

        else {
            LoggerFactory.getLogger(getClass()).error("Message is not a quote: {}", message.getContentRaw());
            this.isTextQuote = false;
            throw new RuntimeException("Unknown message type: " + message);
        }
    }

}
