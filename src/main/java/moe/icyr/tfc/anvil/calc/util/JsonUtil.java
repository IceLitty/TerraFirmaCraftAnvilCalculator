package moe.icyr.tfc.anvil.calc.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.NonNull;
import moe.icyr.tfc.anvil.calc.resource.Recipe;
import moe.icyr.tfc.anvil.calc.resource.ResourceLocation;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author Icy
 * @since 2023/9/13
 */
public class JsonUtil {

    private final JsonMapper jsonMapper;
    public static final JsonMapper INSTANCE = new JsonUtil().jsonMapper;

    private JsonUtil() {
        // 防止double序列化为科学记数法
        SimpleModule simpleModule = new SimpleModule("DoubleSerializer", new Version(1, 0, 0, "", "moe.icyr", ""));
        simpleModule.addSerializer(Double.class, new DoubleSerializer());
        simpleModule.addSerializer(double.class, new DoubleSerializer());
        jsonMapper = (JsonMapper) JsonMapper.builder()
                // json比javaBean多字段时默认会抛出异常，改为不抛出
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // null字段会输出"key":null，改为不输出
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                // 没有字段的javaBean在序列化时会异常，改为不抛出
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                // 允许读单引号模式
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                // 允许读控制字符如\t
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                // 允许读不带引号的key
                .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                // 允许忽略大小写键匹配
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                // 关闭时间字符串转为时间戳输出
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 防止BigDecimal序列化为科学记数法
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                // 关闭空Bean转换异常
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build()
                // 指定时间字符串格式
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .setTimeZone(TimeZone.getTimeZone("GMT+8"))
                // 防止double序列化为科学记数法
                .registerModule(simpleModule)
                ;
    }

    public static class DoubleSerializer extends JsonSerializer<Double> {
        @Override
        public void serialize(Double aDouble, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            BigDecimal bigDecimal = new BigDecimal(aDouble.toString());
            jsonGenerator.writeNumber(bigDecimal.stripTrailingZeros().toPlainString());
        }
    }

    /**
     * 处理资源包文件读取为对象
     *
     * @param namespace    命名空间
     * @param resourcePath 资源路径
     * @param resourceData 资源数据
     * @param resourceType 资源类型
     * @param <T>          资源类型
     * @return 资源对象
     * @throws Exception Just use log.error(e) to print stderr.
     */
    public static <T extends ResourceLocation> T readResourceFromData(String namespace, String resourcePath, byte[] resourceData, @NonNull Class<T> resourceType) throws Exception {
        if (resourceData == null) return null;
        if (resourceType == Recipe.class) {
            try {
                String s = new String(resourceData, StandardCharsets.UTF_8);
                T recipe = JsonUtil.INSTANCE.readValue(s, resourceType);
                recipe.setNamespace(namespace);
                recipe.setPath(resourcePath);
                return recipe;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Json process error found on " + namespace + ":" + resourcePath + " recipe file.", e);
            }
        } else {
            // TODO else assets etc...
            throw new UnsupportedOperationException("Resource type " + resourceType.getName() + " is not supported.");
        }
    }

}
