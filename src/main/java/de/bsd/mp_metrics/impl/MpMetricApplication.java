package de.bsd.mp_metrics.impl;

import de.bsd.mp_metrics.ApplicationMetrics;
import de.bsd.mp_metrics.demo.DemoBean;
import java.io.InputStream;
import java.net.URL;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.jboss.logging.Logger;

/**
 * @author Heiko W. Rupp
 */
// We use / so that metrics worker and demo can co-exist below it
@ApplicationPath("/")
@ApplicationScoped
public class MpMetricApplication extends Application {

    Logger log = Logger.getLogger(this.getClass().getName());


    public MpMetricApplication() {

        log.info("Start MpMetricApplication...");

        URL url = getClass().getResource(".");
        log.info(url.toExternalForm());
        InputStream is  = getClass().getResourceAsStream("../mapping.yml"); // TODO check with other dir
        if (is == null) {
            log.warn("IS is null");
        }


        ConfigReader cr = new ConfigReader();
        Metadata config;
        if (is!=null) {
            config = cr.readConfig(is);
        } else {
            String mappingFile = System.getProperties().getProperty("mapping", "de/bsd/mp_metrics/mapping.yml");
            config = cr.readConfig(mappingFile);
        }
        ConfigHolder.getInstance().setConfig(config);

        // Register metrics
        DemoBean.registerMetricsForDemoBean();


    }


    // Expose the app (meta data holder to applications)
    @Produces
    public ApplicationMetrics setupApplicationMetrics() {
        return ApplicationMetrics.getInstance();
    }


}