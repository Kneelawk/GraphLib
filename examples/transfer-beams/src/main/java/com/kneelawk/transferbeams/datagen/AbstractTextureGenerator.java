package com.kneelawk.transferbeams.datagen;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;

import org.slf4j.Logger;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractTextureGenerator implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final PackOutput.PathProvider resolver;

    public AbstractTextureGenerator(FabricDataOutput output) {
        this.resolver = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures");
    }

    public abstract void generate(TextureGenerator gen);

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        List<CompletableFuture<?>> instances = new ArrayList<>();

        generate((id, width, height, creator) -> {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            creator.draw(image.createGraphics());
            instances.add(writeToPath(writer, image, resolver.file(id, "png")));
        });

        return CompletableFuture.allOf(instances.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Textures";
    }

    protected static CompletableFuture<?> writeToPath(CachedOutput writer, BufferedImage image, Path path) {
        return CompletableFuture.runAsync(() -> {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 HashingOutputStream hos = new HashingOutputStream(Hashing.sha1(), baos)) {

                ImageIO.write(image, "png", hos);

                writer.writeIfNeeded(path, baos.toByteArray(), hos.hash());
            } catch (IOException e) {
                LOGGER.error("Failed to save file to {}", path, e);
            }
        }, Util.backgroundExecutor());
    }

    protected static BufferedImage loadTexture(ResourceLocation id) {
        ClassLoader loader = AbstractTextureGenerator.class.getClassLoader();
        String path = "assets/" + id.getNamespace() + "/textures/" + id.getPath() + ".png";
        URL url = loader.getResource(path);
        if (url == null) throw new RuntimeException("Unable to find image: " + path);

        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface TextureGenerator {
        void addTexture(ResourceLocation id, int width, int height, TextureCreator creator);

        default void addTexture(ResourceLocation id, TextureCreator creator) {
            addTexture(id, 16, 16, creator);
        }
    }

    @FunctionalInterface
    public interface TextureCreator {
        void draw(Graphics2D gfx);
    }
}
