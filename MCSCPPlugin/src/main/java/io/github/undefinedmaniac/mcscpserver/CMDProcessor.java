package io.github.undefinedmaniac.mcscpserver;

import io.github.undefinedmaniac.mcscpplugin.MCSCPPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

//Processes commands received by MCSCPServer in the main thread, allowing for access to the Bukkit API
public class CMDProcessor extends BukkitRunnable {

    private final String mCmd;
    private final MCSCPServer mServer;

    private static CustomCommandSender mSender = new CustomCommandSender();
    private static Pattern mCommandPattern = Pattern.compile("^CMD:(.+)");
    private static Pattern mPlayerReportPattern = Pattern.compile("^GETPLAYERREPORT:(.+)");

    CMDProcessor(String cmd, MCSCPServer server) {
        mCmd = cmd;
        mServer = server;
    }

    public void run() {
        switch (mCmd) {
            case "GETMAXPLAYERS":
                mServer.sendMsgToClient("MAXPlAYERS:" + Bukkit.getServer().getMaxPlayers());
                break;
            case "GETPLAYERCOUNT":
                mServer.sendMsgToClient("PLAYERCOUNT:" + Bukkit.getServer().getOnlinePlayers().size());
                break;
            case "GETPLAYERLIST":
                getPlayerList();
                break;
            case "GETTPS":
                mServer.sendMsgToClient("TPS:" + MCSCPPlugin.getTPS());
                break;
            case "GETTOTALRAM":
                mServer.sendMsgToClient("TOTALRAM:" + Runtime.getRuntime().totalMemory() / 1048576L + "MB");
                break;
            case "GETFREERAM":
                mServer.sendMsgToClient("FREERAM:" + Runtime.getRuntime().freeMemory() / 1048576L + "MB");
                break;
            case "GETUSEDRAM":
                mServer.sendMsgToClient("USEDRAM:" + (Runtime.getRuntime().totalMemory() -
                        Runtime.getRuntime().freeMemory()) / 1048576L + "MB");
                break;
            case "GETMOTD":
                mServer.sendMsgToClient("MOTD:" + Bukkit.getServer().getMotd());
                break;
            case "STOP":
                mServer.sendMsgToClient("STOPACCEPTED");
                Bukkit.getServer().shutdown();
                break;
            default:
                Matcher matcher = mCommandPattern.matcher(mCmd);
                if (matcher.matches()) {
                    consoleCommand(matcher.group(1));
                }
                matcher = mPlayerReportPattern.matcher(mCmd);
                if (matcher.matches()) {
                    getPlayerReport(matcher.group(1));
                }
        }
        cancel();
    }

    private void getPlayerList() {
        StringBuilder response = new StringBuilder();
        response.append("BEGINPLAYERLIST");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            response.append("\r\n");
            response.append(player.getPlayerListName());
        }
        response.append("\r\n");
        response.append("ENDPLAYERLIST");
        mServer.sendMsgToClient(response.toString());
    }

    private void consoleCommand(String command) {
        Bukkit.getServer().dispatchCommand(mSender, command);
        LinkedList<String> messages = mSender.retrieveMessages();
        StringBuilder response = new StringBuilder();
        response.append("CMDRESPONSE:");
        for (String message : messages) {
            response.append(message);
        }
        mServer.sendMsgToClient(response.toString());
    }

    private void getPlayerReport(String playerName) {
        StringBuilder response = new StringBuilder();
        response.append("BEGINPLAYERREPORT");
        response.append("\r\n");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getPlayerListName().equals(playerName)) {
                response.append("NAME:");
                response.append(playerName);
                response.append("\r\n");
                response.append("IP:");
                response.append(player.getAddress().toString());
                response.append("\r\n");
                response.append("MAXHEALTH:");
                response.append(player.getMaxHealth());
                response.append("\r\n");
                response.append("HEALTH:");
                response.append(player.getHealth());
                response.append("\r\n");
                response.append("HUNGER:");
                response.append(player.getFoodLevel());
                response.append("\r\n");
                response.append("LEVEL:");
                response.append(player.getLevel());
                response.append("\r\n");
                response.append("WORLD:");
                response.append(player.getWorld().getName());
                response.append("\r\n");
                break;
            }
        }
        response.append("ENDPLAYERREPORT");
        mServer.sendMsgToClient(response.toString());
    }
}