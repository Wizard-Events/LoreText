package ron.thewizard.loretext;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class Utils {

    public static final TextColor ginkoBlue, wizardPurple, wizardRed, wizardWhite;

    static {
        ginkoBlue = TextColor.fromHexString("#21FFF5");
        wizardRed = TextColor.fromHexString("#FF334E");
        wizardWhite = TextColor.fromHexString("#FFE0E2");
        wizardPurple = TextColor.fromHexString("#B442FF");
    }

    public static List<Component> readUTF8File(File textFile) throws Exception {
        List<Component> deserializedLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                deserializedLines.add(MiniMessage.miniMessage().deserialize(replaceAmpersand(line)));
            }
        }
        return deserializedLines;
    }

    public static String replaceAmpersand(String string) {
        string = string.replace("&0", "<black>");
        string = string.replace("&1", "<dark_blue>");
        string = string.replace("&2", "<dark_green>");
        string = string.replace("&3", "<dark_aqua>");
        string = string.replace("&4", "<dark_red>");
        string = string.replace("&5", "<dark_purple>");
        string = string.replace("&6", "<gold>");
        string = string.replace("&7", "<gray>");
        string = string.replace("&8", "<dark_gray>");
        string = string.replace("&9", "<blue>");
        string = string.replace("&a", "<green>");
        string = string.replace("&b", "<aqua>");
        string = string.replace("&c", "<red>");
        string = string.replace("&d", "<light_purple>");
        string = string.replace("&e", "<yellow>");
        string = string.replace("&f", "<white>");
        string = string.replace("&k", "<obfuscated>");
        string = string.replace("&l", "<bold>");
        string = string.replace("&m", "<strikethrough>");
        string = string.replace("&n", "<underlined>");
        string = string.replace("&o", "<italic>");
        string = string.replace("&r", "<reset>");
        return string;
    }
}
