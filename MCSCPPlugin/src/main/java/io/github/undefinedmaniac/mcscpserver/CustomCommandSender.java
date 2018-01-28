package io.github.undefinedmaniac.mcscpserver;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

//Wrapper for ConsoleCommandSender that allows CMDProcessor to receive console command feedback
class CustomCommandSender implements CommandSender {

    private static ConsoleCommandSender mSender = Bukkit.getConsoleSender();
    private Queue<String> mQueue = new LinkedList<>();

    public LinkedList<String> retrieveMessages() {
        LinkedList<String> messages = new LinkedList<>();
        while (mQueue.peek() != null) {
            messages.add(mQueue.poll());
        }
        return messages;
    }

    @Override
    public void sendMessage(String[] strings) {
        for (String message : strings) {
            sendMessage(message);
        }
    }

    @Override
    public void sendMessage(String s) {
        mQueue.add(s);
        mSender.sendMessage(s);
    }

    @Override
    public void setOp(boolean b) {
        mSender.setOp(b);
    }

    @Override
    public boolean isOp() {
        return mSender.isOp();
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return mSender.hasPermission(permission);
    }

    @Override
    public boolean hasPermission(String s) {
        return mSender.hasPermission(s);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return mSender.isPermissionSet(permission);
    }

    @Override
    public boolean isPermissionSet(String s) {
        return mSender.isPermissionSet(s);
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return mSender.getEffectivePermissions();
    }

    @Override
    public void recalculatePermissions() {
        mSender.recalculatePermissions();
    }

    @Override
    public String getName() {
        return mSender.getName();
    }

    @Override
    public Server getServer() {
        return mSender.getServer();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return mSender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return mSender.addAttachment(plugin, i);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return mSender.addAttachment(plugin, s, b);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return mSender.addAttachment(plugin, s, b, i);
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {
        mSender.removeAttachment(permissionAttachment);
    }
}