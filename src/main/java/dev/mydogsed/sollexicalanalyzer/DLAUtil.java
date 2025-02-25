package dev.mydogsed.sollexicalanalyzer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.util.List;
import java.util.Random;

public class DLAUtil {

    // get message content raw, including forwarded messages
    public static String getMessageContentRaw(Message message){
        if (message.getMessageReference() != null &&
                message.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD
        ) {
            return message.getMessageSnapshots().get(0).getContentRaw();
        }
        else {
            return message.getContentRaw();
        }
    }

    public static Message.Attachment getMessageContentImage(Message message){
        if (message.getMessageReference() != null && // The message has a message reference
                message.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD && // Forwarded Message
                !message.getMessageSnapshots().get(0).getAttachments().isEmpty()
        ) {
            return message.getMessageSnapshots().get(0).getAttachments().get(0);
        }
        else {
            return message.getAttachments().get(0);
        }
    }

    // get message content raw, including forwarded messages
    public static String getMessageContentSanitized(Message message){
        if (message.getMessageReference() != null && message.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD) {
            return MarkdownSanitizer.sanitize(message.getMessageSnapshots().get(0).getContentRaw());
        } else {
            return MarkdownSanitizer.sanitize(message.getContentRaw());
        }
    }

    // If message is forwarded
    public static boolean isForwarded(Message message) {
        if (message.getMessageReference() != null && // The message has a message reference
                message.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD // Forwarded Message
        ) {
            return true;
        }
        return false;
    }

    // return a random keyboard smash
    public static Message randomSmash() {
        List<Message> filtered = Main.smashesCache.getMessages()
                .stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList();
        return filtered.get(new Random().nextInt(filtered.size()));
    }
}
