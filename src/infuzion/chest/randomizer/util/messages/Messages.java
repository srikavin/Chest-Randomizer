package infuzion.chest.randomizer.util.messages;

import infuzion.chest.randomizer.ChestRandomizer;

@SuppressWarnings("WeakerAccess")
public class Messages {
    private final static String errorPrefix = "ChestRandomizationError.";
    private final static String variablePrefix = "Variables.";
    private final static String adminPrefix = "Admin.";
    private final static String helpPrefix = "Help.";
    public static String error_direction;
    public static String error_number;
    public static String error_permission;
    public static String error_player;
    public static String error_unknown;
    public static String error_group;
    public static String error_message;
    public static String error_world;
    public static String help_reload;
    public static String metrics_optout;
    public static String metric_optin;
    public static String updater_optout;
    public static String updater_optin;
    public static String randomize_success;
    public static String reload_success;
    public static String admin_help;
    public static String admin_add_help;
    public static String admin_remove_help;
    public static String admin_remove_prompt;
    public static String admin_remove_success;
    public static String admin_remove_timeout;
    public static String admin_add_success;
    public static String admin_add_noitem;
    public static String variable_prefix;
    public static String variable_servername;
    public static String admin_create_help;
    public static String admin_create_success;
    public static String admin_create_exists;
    public static String help_admin;
    public static String help_randomize;
    public static String help_help;
    public static String help_empty;
    private static messagesManager message;

    public Messages(ChestRandomizer pl) {
        message = pl.getMessagesManager();
        init();
    }

    private void init() {
        error_direction = message.getMessage(errorPrefix + "Direction");
        error_number = message.getMessage(errorPrefix + "Number");
        error_permission = message.getMessage(errorPrefix + "Permission");
        error_player = message.getMessage(errorPrefix + "Player");
        error_unknown = message.getMessage(errorPrefix + "Unknown");
        error_group = message.getMessage(errorPrefix + "Group");
        error_message = message.getMessage(errorPrefix + "Message");
        error_world = message.getMessage(errorPrefix + "World");

        metrics_optout = message.getMessage("Metrics.OptOut");
        metric_optin = message.getMessage("Metrics.OptIn");
        updater_optout = message.getMessage("Updater.OptOut");
        updater_optin = message.getMessage("Update.OptIn");

        randomize_success = message.getMessage("Randomize.Success");
        reload_success = message.getMessage("Reload.Success");

        help_reload = message.getMessage(helpPrefix + "Reload");
        help_randomize = message.getMessage(helpPrefix + "Randomize");
        help_admin = message.getMessage(helpPrefix + "Admin");
        help_help = message.getMessage(helpPrefix + "Help");
        help_empty = message.getMessage(helpPrefix + "Empty");

        admin_help = message.getMessage(adminPrefix + "Help");
        admin_add_help = message.getMessage(adminPrefix + "Add.Help");
        admin_remove_help = message.getMessage(adminPrefix + "Remove.Help");
        admin_remove_prompt = message.getMessage(adminPrefix + "Remove.Prompt");
        admin_remove_success = message.getMessage(adminPrefix + "Remove.Success");
        admin_remove_timeout = message.getMessage(adminPrefix + "Remove.Timeout");
        admin_add_success = message.getMessage(adminPrefix + "Add.Success");
        admin_add_noitem = message.getMessage(adminPrefix + "Add.NoItem");
        admin_create_help = message.getMessage(adminPrefix + "Create.Help");
        admin_create_exists = message.getMessage(adminPrefix + "Create.Exists");
        admin_create_success = message.getMessage(adminPrefix + "Create.Success");

        variable_prefix = message.getMessage(variablePrefix + "Prefix");
        variable_servername = message.getMessage(variablePrefix + "ServerName");
    }

}

