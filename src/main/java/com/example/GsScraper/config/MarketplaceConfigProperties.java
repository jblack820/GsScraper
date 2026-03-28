package com.example.GsScraper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "marketplace")
public class MarketplaceConfigProperties {

    private MarketplaceProperties gsfanatic;
    private MarketplaceProperties hardverapro;

    public MarketplaceProperties getGsfanatic() {
        return gsfanatic;
    }

    public void setGsfanatic(MarketplaceProperties gsfanatic) {
        this.gsfanatic = gsfanatic;
    }

    public MarketplaceProperties getHardverapro() {
        return hardverapro;
    }

    public void setHardverapro(MarketplaceProperties hardverapro) {
        this.hardverapro = hardverapro;
    }
}
