package io.github.kynmarsher.webserviceback.util;

import io.github.kynmarsher.webserviceback.WebServiceBack;
import lombok.SneakyThrows;

import java.io.StringWriter;

public class Utils {

    @SneakyThrows
    public static String dataToJson(Object data) {
        StringWriter sw = new StringWriter();
        WebServiceBack.RESPONSE_MAPPER.writeValue(sw, data);
        return sw.toString();
    }

}
