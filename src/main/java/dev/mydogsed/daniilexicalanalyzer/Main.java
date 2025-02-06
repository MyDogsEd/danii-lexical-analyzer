package dev.mydogsed.daniilexicalanalyzer;

import dev.mydogsed.daniilexicalanalyzer.commands.LexicalCommands;
import dev.mydogsed.daniilexicalanalyzer.commands.MiscCommands;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.MessageCache;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.RegistrySlashCommandListener;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.CommandRegistry;
import dev.mydogsed.daniilexicalanalyzer.commands.framework.SimpleSlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

public class Main extends ListenerAdapter {

    public static JDA jda;

    public static Logger logger = LoggerFactory.getLogger(Main.class);

    public static CommandRegistry commandRegistry = CommandRegistry.getInstance();

    public static MessageCache messageCache;

    public static void main(String[] args) {
        // Log the bot in
        try {
            jda = JDABuilder.createDefault(getApiKey())
                    .addEventListeners(new Main())
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .build();
        }
        // File not found
        catch (FileNotFoundException e) {
            logger.error("API Key file not found.");
            logger.error("You must create the BOT_KEY.apikey file in the same directory as the .jar file.");
            logger.error("(Checking in {} for key file)", Paths.get("").toAbsolutePath());
        }
        // Token is not valid
        catch (InvalidTokenException e) {
            logger.error("The provided token is invalid.");
        }
        //
        catch (IllegalArgumentException e){
            logger.error("One of the provided arguments is invalid.");
        }
        // The bot has already started at this point, so all code is handled by events
    }

    // Utility method to get the API key from the file present in the same directory
    public static String getApiKey() throws FileNotFoundException {
        File file = new File("./BOT_KEY.apikey");
        Scanner scanner = new Scanner(file);
        return scanner.next();
    }

    // Register all commands and things after the bot is logged in and ready for us to do so
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("Starting danii-lexical-analyzer on JDA version " + JDAInfo.VERSION);
        // TODO: move this to a command or something, this really should only be done once, not every time the bot logs in
        registerCommandExecutors();
        registerSlashCommands();
        registerListeners();

        // TODO: Set the bot's status to yellow or red on startup, then to green when it is actually ready to accept commands

        // ALSO TODO: scout tf2 voicelines or something witty and funny for the bot's status

        // Create the message cache for the #quotes-without-context channel

        logger.info("danii-lexical-analyzer is ready!");
    }

    // Register the slash commands to discord
    public static void registerSlashCommands(){
        //Main.jda.updateCommands()
        // Hardcoded id for fruity factory and testing guild
        // MyDogsBot guild: 734502410952769607
        // Fruity Factory: 1233092684198182943

        // Register slash commands for the two guilds:
        try {
            registerCommandsForGuild(Objects.requireNonNull(jda.getGuildById("734502410952769607")));
            registerCommandsForGuild(Objects.requireNonNull(jda.getGuildById("1233092684198182943")));
        } catch (NullPointerException e) {
            logger.error("Guilds not found for registering slash commands!");
        }
        logger.info("Registered Slash Commands");


    }

    // For the given guild, register all commands in the command registry as slash commands for that guild
    private static void registerCommandsForGuild(Guild guild){
        CommandRegistry registry = CommandRegistry.getInstance();
        Set<String> commandNames = registry.getCommandNames();
        CommandListUpdateAction updateAction = guild.updateCommands();
        for(String commandName : commandNames){
            updateAction = updateAction.addCommands(
                    Commands.slash(commandName, registry.getExecutor(commandName).getDescription())
                            .setGuildOnly(true)
            );
        }
        updateAction.queue();
    }

    // Register the command Executors so the commands actually do something lmao
    public static void registerCommandExecutors(){

        // Register simple slash commands
        commandRegistry.register("hello", new SimpleSlashCommand("Hello!",
                "Sends a hello message!"));
        commandRegistry.register("invite", new SimpleSlashCommand("Invite the bot here: " +
                "https://discord.com/oauth2/authorize?client_id=1294039897316917278&permissions=8&scope=bot",
                "Prints the invite link for the bot to the chanel!"));

        // Register classes that use the @SlashCommand decorators
        commandRegistry.registerMethods(LexicalCommands.class);
        commandRegistry.registerMethods(MiscCommands.class);

        // Register Classes that implement SlashCommand

        // Log that command executors have been registered
        logger.info("Registered Command Executors");
    }

    public static void registerListeners(){
        Main.jda.addEventListener(new RegistrySlashCommandListener());
        logger.info("Registered Event Listeners");
    }
}
