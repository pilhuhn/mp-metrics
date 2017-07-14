package de.bsd.mp_metrics.impl;

import de.bsd.mp_metrics.ApplicationMetrics;
import de.bsd.mp_metrics.MetadataEntry;
import de.bsd.mp_metrics.demo.DemoBean;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
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

    private Logger log = Logger.getLogger(this.getClass().getName());


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

        ExtMetadata superConfig = flattenIntegrations(config);
        postProcessConfig(superConfig);

        ConfigHolder.getInstance().setConfig(superConfig);

        // Register metrics
        DemoBean.registerMetricsForDemoBean();


    }

    /**
     * The integration sub-space has a format where metrics are grouped
     * below subsystems as e.g.
     * <pre>
     integration:
       servlet:
       - name: "Servlet bla %s"
         description: Bla for servlet %s
       ft:
       - name: "ftbla"
         description: Bla for ft %s
     * </pre>
     * with two subsystems of 'servlet' and 'ft'.
     *
     * Flattening now takes the subsystem and creates
     * entries for its metrics with the subsystem name prefixed
     *
     * @param config The configuration read in from the config file
     * @return a Flattened config
     */
    private ExtMetadata flattenIntegrations(Metadata config) {
        List<MetadataEntry> out = new ArrayList<>();
        ExtMetadata sm = new ExtMetadata(config);

        Map<String, List<Map<String, Object>>> map = config.getIntegration();
        for (String key : map.keySet()) {
            List<Map<String,Object>> entries = map.get(key);
            for (Map entry : entries) {
                try {
                    MetadataEntry me = new MetadataEntry(entry);
                    me.setName(key + "_" + me.getName());
                    out.add(me);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        sm.setIntegrations(out);

        return sm;
    }

    private void postProcessConfig(Metadata config) {
        final String[] bases = {"base","vendor", "integration" };

        for (String base : bases) {
            List<MetadataEntry> entries = config.get(base);
            expandMultiValueEntries(entries);
        }
    }

    /**
     * We need to expand entries that are marked with the <b>multi</b> flag
     * into the actual MBeans. This is done by replacing a placeholder of <b>%s</b>
     * in the name and MBean name with the real Mbean key-value.
     * @param entries
     */
    private void expandMultiValueEntries(List<MetadataEntry> entries) {
        List<MetadataEntry> result = new ArrayList<>();
        List<MetadataEntry> toBeRemoved = new ArrayList<>(entries.size());
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        for (MetadataEntry entry : entries) {
            if (entry.isMulti()) {
                String name = entry.getMbean().replace("%s", "*");
                String attName = null;
                String queryableName = null;
                int slashIndex = name.indexOf('/');

                // MBeanName is invalid, lets skip this alltogether
                if (slashIndex<0) {
                    toBeRemoved.add(entry);
                    continue;
                }

                queryableName = name.substring(0, slashIndex);
                attName = name.substring(slashIndex +1);

                ObjectName objectName = null;
                try {
                    objectName = new ObjectName(queryableName);

                    String keyHolder = findKeyForValueToBeReplaced(objectName);

                    Set<ObjectName> objNames = mbs.queryNames(objectName, null);
                    for (ObjectName oName : objNames) {
                        String keyValue = oName.getKeyPropertyList().get(keyHolder);
                        String newName = entry.getName().replace("%s",keyValue);
                        String newDisplayName = entry.getDisplayName().replace("%s",keyValue);
                        String newDescription = entry.getDescription().replace("%s",keyValue);
                        MetadataEntry newEntry = new MetadataEntry(newName, newDisplayName, newDescription,
                                                                   entry.getTypeRaw(), entry.getUnitRaw());
                        newEntry.setTags(entry.getTagsForEntryOnly());
                        String newObjectName = oName.getCanonicalName() + "/" + attName;
                        newEntry.setMbean(newObjectName);
                        result.add(newEntry);
                    }
                    toBeRemoved.add(entry);
                } catch (MalformedObjectNameException e) {
                    e.printStackTrace();  // TODO: Customise this generated block
                }
            }
        }
        entries.removeAll(toBeRemoved);
        entries.addAll(result);
        log.info("Converted [" + toBeRemoved.size() + "] config entries and added [" + result.size() + "] replacements");
    }

    private String findKeyForValueToBeReplaced(ObjectName objectName) {
        String keyHolder = null;
        Hashtable<String,String> keyPropList =  objectName.getKeyPropertyList();
        for (String key : keyPropList.keySet()) {
            if (keyPropList.get(key).equals("*")) {
                keyHolder = key;
            }
        }
        return keyHolder;
    }


    // Expose the app (meta data holder to applications)
    @Produces
    public ApplicationMetrics setupApplicationMetrics() {
        return ApplicationMetrics.getInstance();
    }


}
