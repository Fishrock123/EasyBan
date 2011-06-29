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

import uk.org.whoami.easyban.util.Subnet;

public interface Datasource {
    public void addIpToHistory(String nick,String ip);

    public void banNick(String nick,String admin);
    public void banNick(String nick,String admin,String reason);
    public void unbanNick(String nick);

    public void banSubnet(Subnet subnet,String admin);
    public void banSubnet(Subnet subnet,String admin,String reason);
    public void unbanSubnet(Subnet subnet);

    public boolean isIpBanned(String ip);
    public boolean isNickBanned(String nick);

    public String[] getBannedNicks();
    public String[] getBannedSubnets();
    public String[] getPlayerIps(String nick);
    public String[] getBanInformation(String nick);
    public String[] getBanInformation(Subnet subnet);

    public void close();

}
