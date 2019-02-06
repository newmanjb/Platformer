package com.noomtech.jsw.common.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class GlobalConfig extends Properties {


    private GlobalConfig() {
        try {
            this.load(new FileReader(System.getProperty("config") + File.separator + "global_config.cfg"));
        }
        catch(IOException e) {
            System.out.println("Could not load config");
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static final class INSTANCE_HOLDER {
        private static final GlobalConfig INSTANCE = new GlobalConfig();
    }


    public static GlobalConfig getInstance() {
        return INSTANCE_HOLDER.INSTANCE;
    }
}
