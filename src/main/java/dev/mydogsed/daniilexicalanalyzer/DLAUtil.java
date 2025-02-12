package dev.mydogsed.daniilexicalanalyzer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

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
                message.getMessageSnapshots().get(0).getAttachments().size() == 1
        ) {
            return message.getMessageSnapshots().get(0).getAttachments().get(0);
        }
        else {
            return message.getAttachments().get(0).getUrl();
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
}
