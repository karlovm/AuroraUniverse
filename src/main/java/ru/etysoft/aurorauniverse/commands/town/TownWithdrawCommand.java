package ru.etysoft.aurorauniverse.commands.town;

import org.bukkit.command.CommandSender;
import ru.etysoft.aurorauniverse.data.Messages;
import ru.etysoft.aurorauniverse.data.Towns;
import ru.etysoft.aurorauniverse.utils.AuroraConfiguration;
import ru.etysoft.aurorauniverse.utils.Messaging;
import ru.etysoft.aurorauniverse.utils.Permissions;
import ru.etysoft.aurorauniverse.world.Resident;
import ru.etysoft.aurorauniverse.world.Town;

public class TownWithdrawCommand {
    public TownWithdrawCommand(String[] args, Resident resident, CommandSender sender) {
        if (args.length > 1) {
            if (resident == null) {
                if (args.length > 2) {
                    Town town = Towns.getTown(args[1]);
                    if (town == null) {
                        Messaging.mess(Messages.wrongArgs(), sender);
                    } else {
                        try {
                            double towithdraw = Double.valueOf(args[2]);
                            town.withdrawBank(towithdraw);
                        } catch (Exception e) {
                            Messaging.mess(Messages.wrongArgs(), sender);
                        }
                    }
                } else {
                    Messaging.mess(Messages.wrongArgs(), sender);
                }
            } else {
                if (resident.hasTown()) {
                    if (Permissions.canWithdrawTown(sender)) {
                        Town t = resident.getTown();
                        double d = 0;
                        try {
                            d = Double.valueOf(args[1]);

                        } catch (Exception e) {
                            Messaging.mess(AuroraConfiguration.getColorString("no-arguments"), sender);
                        }

                        if (t.withdrawBank(d)) {
                            Messaging.mess(AuroraConfiguration.getColorString("town-withdraw").replace("%s", d + ""), sender);
                            resident.giveBalance(d);
                        } else {
                            Messaging.mess(AuroraConfiguration.getColorString("town-cantwithdraw").replace("%s", d + ""), sender);
                        }
                    } else {
                        Messaging.mess(AuroraConfiguration.getColorString("access-denied-message"), sender);
                    }
                } else {
                    Messaging.mess(AuroraConfiguration.getColorString("town-dont-belong"), sender);
                }
            }
        } else {
            Messaging.mess(AuroraConfiguration.getColorString("no-arguments"), sender);
        }
    }
}
