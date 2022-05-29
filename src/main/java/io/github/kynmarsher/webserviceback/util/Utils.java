package io.github.kynmarsher.webserviceback.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import io.github.kynmarsher.webserviceback.WebServiceBack;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.StringWriter;

@UtilityClass
public class Utils {
    private static char[] ID_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

    @SneakyThrows
    public static String dataToJson(Object data) {
        StringWriter sw = new StringWriter();
        WebServiceBack.RESPONSE_MAPPER.writeValue(sw, data);
        return sw.toString();
    }

    public static String nanoId() {
        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, ID_ALPHABET, 6);
    }

}
