/*
 * Copyright 2011 Sebastian Köhler <sebkoehler@whoami.org.uk>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.org.whoami.easyban.settings.Settings;
import uk.org.whoami.easyban.util.ConsoleLogger;

public class KickCommand extends EasyBanCommand {

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
            return;
        }

        Player player = cs.getServer().getPlayer(args[0]);
        if (player != null) {
            String name = player.getDisplayName();
            
            String kickmsg = m._("You have been kicked");
            String reason = "";
            if(args.length > 1) {
                kickmsg += " " + m._("Reason: ");
                for (int i = 1; i < args.length; i++) {
                    kickmsg += args[i] + " ";
                    reason += args[i] + " ";
                }
            }
            
            if(Settings.getInstance().isAppendCustomKickMessageEnabled()) {
                kickmsg += " " + m._("custom_kick");
            }
            
            player.kickPlayer(kickmsg);
            Settings settings = Settings.getInstance();
            if(settings.isKickPublic()) {
                cs.getServer().broadcastMessage(name + m._(" has been kicked"));
                if(!reason.isEmpty() && settings.isKickReasonPublic()) {
                    cs.getServer().broadcastMessage(name + m._("Reason: ") + reason);
                }
            }
            ConsoleLogger.info(player.getName() + " has been kicked by " + admin);
        }
    }
}
