package ua.caunt.bungeeforge.mixin.server.network;

import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Duration;

@Mixin(net.minecraft.server.network.ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImpl {
    @Shadow
    private RemoteChatSession chatSession;

    @Inject(method = "handleChatSessionUpdate(Lnet/minecraft/network/protocol/game/ServerboundChatSessionUpdatePacket;)V", at = @At("HEAD"))
    public void bungee$handleChatSessionUpdate(ServerboundChatSessionUpdatePacket p_253950_, CallbackInfo ci) throws ProfilePublicKey.ValidationException {
        var remoteChatSessionData = p_253950_.chatSession();

        var sessionId = remoteChatSessionData.sessionId();
        var profilePublicKey = ProfilePublicKey.createValidated(SignatureValidator.NO_VALIDATION, sessionId, remoteChatSessionData.profilePublicKey(), Duration.ZERO);

        chatSession = new RemoteChatSession(sessionId, profilePublicKey);
    }
}
