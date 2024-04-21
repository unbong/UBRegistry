package io.unbong.ubregistry.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * instance meta model
 *
 * @author <a href="ecunbong@gmail.com">unbong</a>
 * 2024-04-13 20:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"schema", "host", "port", "context"})
public class InstanceMeta {

    public InstanceMeta(String schema, String host, Integer port, String context ) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    private String schema;  // protocol default http
    private String host;
    private Integer port;
    private String context;

    private boolean status;     // online offline
    private Map<String,String> parameters = new HashMap<>(); // which server room ...

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public static InstanceMeta http(String host, Integer port){
        return new InstanceMeta("http", host, port, "ubrpc");
    }

    public String toURL() {
        return String.format("%s://%s:%d/%s", schema, host, port,context);
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }

    public InstanceMeta addMeta(Map<String,String> params ) {
        this.parameters.putAll(params);
        return  this;
    }

   public static InstanceMeta from (String url){
        URI uri = URI.create(url);
        return new InstanceMeta(uri.getScheme(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath().substring(1));
   }

}
