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
package uk.org.whoami.easyban.datasource;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.java.JavaPlugin;

import uk.org.whoami.easyban.ConsoleLogger;

public class HSQLDataSource extends SQLDataSource {

    private String path;

    public HSQLDataSource(JavaPlugin plugin) throws ClassNotFoundException,
            SQLException {
        this.path = plugin.getDataFolder().getAbsolutePath() + "/bans";
        connect();
        setup();
        ConsoleLogger.info("Database setup finished");
    }

    @Override
    final protected synchronized void connect() throws SQLException,
            ClassNotFoundException {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        ConsoleLogger.info("HSQLDB driver loaded");
        con = DriverManager.getConnection("jdbc:hsqldb:file:" + path, "SA", "");
        ConsoleLogger.info("Connected to Database");
    }

    @Override
    final protected synchronized void setup() throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `player` ("
                    + "`player_id` INTEGER GENERATED BY DEFAULT AS IDENTITY,"
                    + "`player` VARCHAR(20) NOT NULL,"
                    + "CONSTRAINT `player_const_prim` PRIMARY KEY (`player_id`),"
                    + "CONSTRAINT `player_const_uniq` UNIQUE(`player`)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `ip` ("
                    + "`ip_id` INTEGER GENERATED BY DEFAULT AS IDENTITY,"
                    + "`player_id` INTEGER NOT NULL,"
                    + "`ip` VARCHAR(40) NOT NULL,"
                    + "CONSTRAINT `ip_const_prim` PRIMARY KEY (`ip_id`),"
                    + "CONSTRAINT `ip_const_unique` UNIQUE (`player_id`, `ip`),"
                    + "CONSTRAINT `ip_const_ref` FOREIGN KEY (`player_id`) REFERENCES `player` (`player_id`)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `player_ban` ("
                    + "`player_ban_id` INTEGER GENERATED BY DEFAULT AS IDENTITY,"
                    + "`player_id` INTEGER NOT NULL,"
                    + "`admin` VARCHAR(255) NOT NULL,"
                    + "`reason` VARCHAR(255),"
                    + "`until` TIMESTAMP,"
                    + "CONSTRAINT `player_ban_const_prim` PRIMARY KEY (`player_ban_id`),"
                    + "CONSTRAINT `player_ban_const_uniq` UNIQUE (`player_id`),"
                    + "CONSTRAINT `player_ban_const_ref` FOREIGN KEY (`player_id`) REFERENCES player (`player_id`)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `subnet_ban` ("
                    + "`subnet_ban_id` INTEGER GENERATED BY DEFAULT AS IDENTITY,"
                    + "`subnet` VARCHAR(100) NOT NULL,"
                    + "`admin` VARCHAR(255) NOT NULL,"
                    + "`reason` VARCHAR(255),"
                    + "CONSTRAINT `subnet_ban_const_prim` PRIMARY KEY (`subnet_ban_id`),"
                    + "CONSTRAINT `subnet_ban_const_uniq` UNIQUE (`subnet`)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `country_ban` ("
                    + "`country_ban_id` INTEGER GENERATED BY DEFAULT AS IDENTITY,"
                    + "`country` VARCHAR(2) NOT NULL,"
                    + "CONSTRAINT `country_ban_const_prim` PRIMARY KEY (`country_ban_id`),"
                    + "CONSTRAINT `country_ban_const_uniq` UNIQUE (`country`)"
                    + ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `whitelist` ("
                    + "`whitelist_id` INTEGER GENERATED BY DEFAULT AS IDENTITY,"
                    + "`player_id` INTEGER NOT NULL,"
                    + "CONSTRAINT `whitelist_const_prim` PRIMARY KEY (`whitelist_id`),"
                    + "CONSTRAINT `whitelist_const_uniq` UNIQUE (`player_id`),"
                    + "CONSTRAINT `whitelist_const_ref` FOREIGN KEY (`player_id`) REFERENCES `player` (`player_id`)"
                    + ");");

            //update old tables
            rs = con.getMetaData().getIndexInfo(con.getCatalog(), null, "ip", true, true);
            while (rs.next()) {
                if(rs.getString("INDEX_NAME").equalsIgnoreCase("ip_const_uniq")) {
                    return;
                }
            }
            st.executeUpdate("ALTER TABLE `player` ALTER COLUMN `player` VARCHAR(255);");
            st.executeUpdate("ALTER TABLE `ip` ADD CONSTRAINT `ip_const_uniq` UNIQUE (`player_id`, `ip`);");
            st.executeUpdate("ALTER TABLE `player_ban` ALTER COLUMN `admin` VARCHAR(255);");
            st.executeUpdate("ALTER TABLE `player_ban` ALTER COLUMN `reason` VARCHAR(255);");
            st.executeUpdate("ALTER TABLE `subnet_ban` ALTER COLUMN `admin` VARCHAR(255);");
            st.executeUpdate("ALTER TABLE `subnet_ban` ALTER COLUMN `reason` VARCHAR(255);");
            st.executeUpdate("ALTER TABLE `whitelist` ADD CONSTRAINT `whitelist_const_uniq` UNIQUE (`player_id`);");
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public synchronized void close() {
        try {
            Statement st = con.createStatement();
            st.execute("SHUTDOWN");
            con.close();
        } catch (SQLException ex) {
            ConsoleLogger.info(ex.getMessage());
        }
    }
}
