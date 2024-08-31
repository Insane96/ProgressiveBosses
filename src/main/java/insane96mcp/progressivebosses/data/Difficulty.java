package insane96mcp.progressivebosses.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

import java.lang.reflect.Type;

@JsonAdapter(Difficulty.Serializer.class)
public class Difficulty {
    public float easy;
    public float normal;
    public float hard;

    public Difficulty(float easy, float normal, float hard) {
        this.easy = easy;
        this.normal = normal;
        this.hard = hard;
    }

    public static Difficulty of(float easy, float normal, float hard) {
        return new Difficulty(easy, normal, hard);
    }

    public int getInt(Level level) {
        return (int) getFloat(level);
    }

    public float getFloat(Level level) {
        if (level.getDifficulty() == net.minecraft.world.Difficulty.EASY)
            return easy;
        else if (level.getDifficulty() == net.minecraft.world.Difficulty.NORMAL)
            return normal;
        else
            return hard;
    }

    public static class Serializer implements JsonSerializer<Difficulty>, JsonDeserializer<Difficulty> {
        @Override
        public Difficulty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new Difficulty(GsonHelper.getAsFloat(jObject, "easy"), GsonHelper.getAsFloat(jObject, "normal"), GsonHelper.getAsFloat(jObject, "hard"));
        }

        @Override
        public JsonElement serialize(Difficulty src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("easy", context.serialize(src.easy));
            jsonObject.add("normal", context.serialize(src.normal));
            jsonObject.add("hard", context.serialize(src.hard));
            return jsonObject;
        }
    }
}
