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
package uk.org.whoami.easyban.listener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import uk.org.whoami.easyban.ConsoleLogger;
import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.easyban.settings.Message;
import uk.org.whoami.easyban.settings.Settings;
import uk.org.whoami.easyban.util.DNSBL;

public class EasyBanPlayerListener implements Listener {

    private DataSource database;
    private DNSBL dnsbl;
    private Message msg = Message.getInstance();

    public EasyBanPlayerListener(DataSource database, DNSBL dnsbl) {
        this.database = database;
        this.dnsbl = dnsbl;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer() == null || !event.getResult().equals(Result.ALLOWED)) {
            return;
        }

        String name = event.getPlayer().getName();
        String ip = event.getKickMessage();

        database.addIpToHistory(name, ip);

        if (database.isNickBanned(name)) {
            HashMap<String, String> banInfo = database.getBanInformation(name);
            String kickmsg = msg._("You have been banned by ") + banInfo.get("admin");

            if(banInfo.containsKey("reason")) {
                kickmsg += " " + msg._("Reason: ") + banInfo.get("reason");
            }

            if(banInfo.containsKey("until")) {
                Long unixTime = Long.parseLong(banInfo.get("until"));
                kickmsg += " " + msg._("Until: ") + DateFormat.getDateTimeInstance().format(new Date(unixTime));
            }

            if(Settings.getInstance().isAppendCustomBanMessageEnabled()) {
                kickmsg += " " + msg._("custom_ban");
            }

            event.disallow(Result.KICK_BANNED, kickmsg);
            ConsoleLogger.info("Ban for " + name + " detected");
            return;
        }


        if (database.isIpBanned(ip)) {
            event.disallow(Result.KICK_BANNED, msg._("You are banned"));
            ConsoleLogger.info("IP Ban for " + name + " detected");
            return;
        }

        if (database.isNickWhitelisted(event.getPlayer().getName())) {
            ConsoleLogger.info("Whitelist entry for " + name + " found");
            return;
        }

        if(dnsbl.isBlocked(ip)) {
            event.disallow(Result.KICK_BANNED, msg._("DNSBL Ban"));
            return;
        }

        if (database.isSubnetBanned(ip)) {
            event.disallow(Result.KICK_BANNED, msg._("Your subnet is banned"));
            ConsoleLogger.info("Subnet ban for " + name + "/" + ip + " detected");
        }
    }

    //This event is only called when some other plugin overwrites the PlayerLogin event
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        String name = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();

        if (database.isNickBanned(name)) {
            HashMap<String, String> banInfo = database.getBanInformation(name);
            String kickmsg = msg._("You have been banned by ") + banInfo.get("admin");

            if(banInfo.containsKey("reason")) {
                kickmsg += " " + msg._("Reason: ") + banInfo.get("reason");
            }

            if(banInfo.containsKey("until")) {
                Long unixTime = Long.parseLong(banInfo.get("until"));
                kickmsg += " " + msg._("Until: ") + DateFormat.getDateTimeInstance().format(new Date(unixTime));
            }

            if(Settings.getInstance().isAppendCustomBanMessageEnabled()) {
                kickmsg += " " + msg._("custom_ban");
            }

            player.kickPlayer(kickmsg);
            ConsoleLogger.info("Ban for " + name + " detected");
            return;
        }


        if (database.isIpBanned(ip)) {
            player.kickPlayer(msg._("You are banned"));
            ConsoleLogger.info("IP Ban for " + name + " detected");
            return;
        }

        if (database.isNickWhitelisted(event.getPlayer().getName())) {
            ConsoleLogger.info("Whitelist entry for " + name + " found");
            return;
        }

        if (database.isSubnetBanned(ip)) {
            player.kickPlayer(msg._("Your subnet is banned"));
            ConsoleLogger.info("Subnet ban for " + name + "/" + ip + " detected");
        }
    }
}
