package com.xorlev.gatekeeper.providers.output;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.xorlev.gatekeeper.AppConfig;
import com.xorlev.gatekeeper.data.ConfigContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ConfigWriter {
    private static final Logger log = LoggerFactory.getLogger(ConfigWriter.class);
    private final Mustache mustache;
    private Writer writer = new OutputStreamWriter(System.out);

    public ConfigWriter(Writer writer) {
        MustacheFactory mf = new DefaultMustacheFactory();
        Reader reader = null;
        try {
            reader = new FileReader(new File(AppConfig.getString("nginx.template-file")));
        } catch (FileNotFoundException e) {
            try {
                InputStream is = this.getClass().getResourceAsStream("nginx.conf.mustache");

                if (is == null) throw e;

                reader = new InputStreamReader(is);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        this.mustache = mf.compile(reader, "nginx_conf");
        this.writer = writer;
    }

    public void writeConfig(ConfigContext configContext) throws IOException {
        String filename = AppConfig.getString("nginx.config-file");

        log.info("Flushing NGINX config to {}", filename);

        writer = new FileWriter(filename);
        mustache.execute(writer, configContext).flush();
    }
}