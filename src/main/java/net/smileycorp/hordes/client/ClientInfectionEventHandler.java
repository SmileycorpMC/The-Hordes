package net.smileycorp.hordes.client;



public class ClientInfectionEventHandler {

	/*@SubscribeEvent
	public <T extends LivingEntity, M extends EntityModel<T>> void  preRenderEntity(RenderLivingEvent.Pre<T, M> event){
		if (ClientConfigHandler.playerInfectionVisuals.get()) {
			Minecraft mc = Minecraft.getInstance();
			PlayerEntity player = mc.player;
			if (player.hasEffect(HordesInfection.INFECTED.get()) && event.getEntity() != player) {
				int a = player.getEffect(HordesInfection.INFECTED.get()).getAmplifier();
				if (a > 2) {
					GlStateManager._logicOp(5391);
					GlStateManager._blendColor(1, 0, 0, 1);
				} else if (a == 2) {
					GlStateManager._logicOp(5385);
					GlStateManager._blendColor(1, 0.4f, 0.4f, 1);
				}
			}
		}
	}

	@SubscribeEvent
	public <T extends LivingEntity, M extends EntityModel<T>> void postRenderEntity(RenderLivingEvent.Post<T, M> event){
		if (ClientConfigHandler.playerInfectionVisuals.get()) {
			Minecraft mc = Minecraft.getInstance();
			PlayerEntity player = mc.player;
			if (player.hasEffect(HordesInfection.INFECTED.get()) && event.getEntity() != player) {
				if (player.getEffect(HordesInfection.INFECTED.get()).getAmplifier() >= 2) {
					GlStateManager._blendColor(1, 1, 1, 1);
					GlStateManager._logicOp(5377);
				}
			}
		}
	}

	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Post event){
		if (ClientConfigHandler.playerInfectionVisuals.get()) {
			Minecraft mc = Minecraft.getInstance();
			PlayerEntity player = mc.player;
			if (player!= null && event.getType() == ElementType.VIGNETTE) {
				if (player.hasEffect(HordesInfection.INFECTED.get())) {
					int level = player.getEffect(HordesInfection.INFECTED.get()).getAmplifier();
					Color colour = new Color(0.4745f, 0.6117f, 0.3961f, 0.04f*level*level);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glDepthMask(false);
					GL11.glDisable(GL11.GL_ALPHA_TEST);
					GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					//AbstractGui.fill(event.getMatrixStack(), 0, 0, mc.screen.width, mc.screen.height, colour.getRGB());
					GL11.glDepthMask(true);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
					GL11.glEnable(GL11.GL_ALPHA_TEST);

				}
			}
		}
	}*/

}
