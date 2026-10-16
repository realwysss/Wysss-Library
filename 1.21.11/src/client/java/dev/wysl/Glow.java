package dev.wysl;

import net.minecraft.resources.Identifier;

public final class Glow {
	public static final String ID = "wysslib";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
