package com.craftmend.openaudiomc.generic.commands.subcommands;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.commands.interfaces.GenericExecutor;
import com.craftmend.openaudiomc.generic.commands.interfaces.SubCommand;
import com.craftmend.openaudiomc.generic.core.interfaces.ConfigurationImplementation;
import com.craftmend.openaudiomc.generic.core.storage.enums.StorageKey;
import com.craftmend.openaudiomc.generic.networking.client.objects.player.ClientConnection;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;

public class AcceptSubCommand extends SubCommand {

    public AcceptSubCommand() {
        super("accept");
    }

    @Override
    public void onExecute(GenericExecutor sender, String[] args) {
        // set the value to true
        ConfigurationImplementation config = OpenAudioMc.getInstance().getConfiguration();
        NetworkingService service = OpenAudioMc.getInstance().getNetworkingService();

        config.setBoolean(StorageKey.LEGAL_ACCEPTED_TOS_AND_PRIVACY, true);
        config.saveAll();

        sender.sendMessage(OpenAudioMc.getInstance().getCommandModule().getCommandPrefix() + "Welcome to OpenAudioMc! you accepted the terms, enjoy the service!");

        service.connectIfDown();

        for (ClientConnection client : service.getClients()) {
            client.publishUrl();
        }
    }
}
