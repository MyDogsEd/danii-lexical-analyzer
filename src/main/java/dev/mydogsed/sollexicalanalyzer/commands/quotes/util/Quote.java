package dev.mydogsed.sollexicalanalyzer.commands.quotes.util;

import dev.mydogsed.sollexicalanalyzer.DLAUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Quote {

    // CONTENT ---

    // If the quote is just a string, this will be the contentRaw.
    // Otherwise, this will be null
    private String contentRaw;

    private String contentDisplay;

    // If the quote is an image or contains an image,
    private String imageURL;

    // META ---

    // The ID of the message
    private final Long messageID;

    // the ID of the quote's submitter
    private final Long authorID;
    private final String authorName;
    private final String authorAvatarURL;

    // The time that the quote was submitted
    private final OffsetDateTime timeCreated;

    // The channel that this quote was taken from (usually #quotes-without-context)
    private final Long channelID;

    private final String jumpURL;

    // INTERNAL ---

    // Whether this is a text or image quote
    private final boolean isTextQuote;

    // The JDA that created this quote
    private final JDA jda;

    public Quote(Message message) {

        this.messageID = message.getIdLong();
        this.authorID = message.getAuthor().getIdLong();
        this.timeCreated = message.getTimeCreated();
        this.channelID = message.getChannel().getIdLong();
        this.jumpURL = message.getJumpUrl();
        this.jda = message.getJDA();
        this.authorName = message.getAuthor().getEffectiveName();
        this.authorAvatarURL = message.getAuthor().getAvatarUrl();

        if (DLAUtil.isTextQuote(message)) {
            this.contentRaw = message.getContentRaw();
            this.contentDisplay = message.getContentDisplay();
            this.isTextQuote = true;
        }

        else if (DLAUtil.isForwardedTextQuote(message)) {
            this.contentRaw = message.getMessageSnapshots().get(0).getContentRaw();
            // MessageSnapshot does not have an m.getContentDisplay() method
            this.contentDisplay = message.getMessageSnapshots().get(0).getContentRaw();
            this.isTextQuote = true;
        }

        else if (DLAUtil.isImageQuote(message)) {
            this.imageURL = message.getAttachments().get(0).getUrl();
            this.isTextQuote = false;
        }

        else if (DLAUtil.isForwardedImageQuote(message)) {
            this.imageURL = message.getMessageSnapshots().get(0).getAttachments().get(0).getUrl();
            this.isTextQuote = false;
        }

        else {
            LoggerFactory.getLogger(getClass()).error("Message is not a quote: {}", message.getContentRaw());
            this.isTextQuote = false;
            throw new RuntimeException("Unknown message type: " + message);
        }
    }

    public boolean isTextQuote() {
        return isTextQuote;
    }

    public boolean isImageQuote() {
        return !isTextQuote;
    }

    public String getContentRaw() {
        return contentRaw;
    }

    public String getContentDisplay() {
        return contentDisplay;
    }

    public String getImageURL() {
        return imageURL;
    }

    public Long getMessageID() {
        return messageID;
    }

    public Message getMessage() {
        return Objects.requireNonNull(
                        jda.getChannelById(MessageChannel.class, this.channelID),
                        "Could not find MessageChannel for Quote.getMessage()"
                )
                .retrieveMessageById(this.messageID)
                .complete();
    }

    public Long getAuthorID() {
        return authorID;
    }

    public User getAuthor() {
        return jda.retrieveUserById(this.authorID).complete();
    }

    public OffsetDateTime getTimeCreated() {
        return timeCreated;
    }

    public String getJumpURL() {
        return jumpURL;
    }

}
