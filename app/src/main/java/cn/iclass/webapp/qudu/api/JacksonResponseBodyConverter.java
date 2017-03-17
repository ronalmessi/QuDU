package cn.iclass.webapp.qudu.api;
import org.codehaus.jackson.map.ObjectReader;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;


/**
 * Created by ronaldong on 2015/12/21.
 */
final class JacksonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final ObjectReader adapter;

    JacksonResponseBodyConverter(ObjectReader adapter) {
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        try {
            String response = value.string();
            return adapter.readValue(response);
        } finally {
            value.close();
        }
    }
}