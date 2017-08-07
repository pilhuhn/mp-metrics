package de.bsd.mp_metrics.impl;

import de.bsd.mp_metrics.ApplicationMetrics;
import de.bsd.mp_metrics.MetadataEntry;
import de.bsd.mp_metrics.MpMUnit;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


/**
 * @author Heiko Rupp
 */
@Path("/metrics")
@Produces("application/json")
public class MpMetricsWorker {

    private static final String APPLICATION = "application";
    private static final String[] bases = {"base","vendor", "integration", APPLICATION};
    private MBeanServer mbs;

    @Inject
    private ApplicationMetrics applicationMetric;

    public MpMetricsWorker() {
        mbs = ManagementFactory.getPlatformMBeanServer();
    }

    @GET
    @Path("/")
    @Produces("application/json")
    public Map<String, Map<String,Number>> getAllValuesJson() {
        Map<String, Map<String,Number>> results = new HashMap<>(bases.length);
        for (String subTree : bases) {
            results.put(subTree, getValuesForSubTreeAsMap(subTree));
        }
        return results;
    }

    @GET
    @Produces("text/plain")
    public String getAllValuesPrometheus() {
        StringBuilder builder = new StringBuilder();
        for (String subTree : bases) {
            builder.append(getValuesForSubTreeAsPromString(subTree));
        }
        return builder.toString();
    }

    @GET
    @Path("/{sub}")
    @Produces("application/json")
    public Map<String, Number> getBaseValuesJson(@PathParam("sub")String sub) {

        validateSub(sub);
        return getValuesForSubTreeAsMap(sub);
    }

    @GET
    @Produces("text/plain")
    @Path("/{sub}")
    public String getBaseValuesPrometheus(@PathParam("sub")String sub) {

        validateSub(sub);
        return getValuesForSubTreeAsPromString(sub);
    }


    @GET
    @Path("/{sub}/{attribute}")
    @Produces("application/json")
    public Map<String,Number> getValueForFieldJson(@PathParam("sub") String sub, @PathParam("attribute") String attribute) {

        List<MetadataEntry> metadata = ConfigHolder.getInstance().getConfig().get(sub);
        Map<String,Number> results = new HashMap<>(1);

        for (MetadataEntry entry : metadata) {
            if (entry.getName().equals(attribute)) {

                Number value;
                if (APPLICATION.equals(sub)) {
                    value = applicationMetric.getValue(attribute);
                } else {
                    value = getValue(entry.getMbean());
                }
                results.put(entry.getName(), value);

                return results;
            }
        }
        throw new NotFoundException(sub + " / "+ attribute);
    }

    @GET
    @Produces("text/plain")
    @Path("/{sub}/{attribute}")
    public String getValueForFieldPrometheus(@PathParam("sub") String sub, @PathParam("attribute") String
        attribute) {

        List<MetadataEntry> metadata = ConfigHolder.getInstance().getConfig().get(sub);
        StringBuilder builder = new StringBuilder();

        for (MetadataEntry entry : metadata) {
            String key = entry.getName();
            if (key.equals(attribute)) {
                String name = sub + ":" + key;

                getPromTypeLine(builder, entry, name);

                Number value;
                if (APPLICATION.equals(sub)) {
                    value = applicationMetric.getValue(attribute);
                } else {
                    value = getValue(entry.getMbean());
                }
                getPromValueLine(builder, entry, name, value);

                return builder.toString();
            }
        }
        throw new NotFoundException(sub + " / "+ attribute);
    }

    @OPTIONS
    @Produces("application/json")
    public Map<String, List<MetadataEntry>> getAllMetadata() {

        Map<String, List<MetadataEntry>> results = new HashMap<>(bases.length);
        for (String subTree : bases) {
            List<MetadataEntry> metadata = ConfigHolder.getInstance().getConfig().get(subTree);

            results.put(subTree, metadata);
        }
        results.put(APPLICATION,getAppMetadata());
        return results;
    }

    private List<MetadataEntry> getAppMetadata() {
        return applicationMetric.getMetadataList();
    }

    @OPTIONS
    @Path("/{sub}")
    @Produces("application/json")
    public List<MetadataEntry> getMetadataForSubTree(@PathParam("sub")String sub) {

        validateSub(sub);
        List<MetadataEntry> metadata;
        if (APPLICATION.equals(sub)) {
            metadata = getAppMetadata();
        } else {
            metadata = ConfigHolder.getInstance().getConfig().get(sub);
        }
        return metadata;
    }

    private Map<String, Number> getValuesForSubTreeAsMap(String sub) {
        List<MetadataEntry> metadata = ConfigHolder.getInstance().getConfig().get(sub);
        Map<String,Number> results = new HashMap<>(metadata.size());

        for (MetadataEntry entry : metadata) {
            Number value;
            String key = entry.getName();
            try {
            if (APPLICATION.equals(sub)) {
                value = applicationMetric.getValue(key);
            }
            else {
                value = getValue(entry.getMbean());
            }
            results.put(key, value);
            }
            catch (Exception ex) {
                System.err.println("Error for " + sub + "/" + entry.getName() + ": " + ex.getMessage() + "\nCause " + ex
                    .getCause());
            }
        }
        return results;
    }

    private String getValuesForSubTreeAsPromString(String sub) {
        List<MetadataEntry> metadata = ConfigHolder.getInstance().getConfig().get(sub);

        StringBuilder builder = new StringBuilder();
        for (MetadataEntry entry : metadata) {
            String key = entry.getName();
            String name = sub + ":" + key;

            getPromTypeLine(builder, entry, name);
            getPromHelpLine(builder, entry, name);

            Number value;

            if (APPLICATION.equals(sub)) {
                value = applicationMetric.getValue(key);
            } else {
                value = getValue(entry.getMbean());
            }

            getPromValueLine(builder, entry, name, value);
        }
        return builder.toString();
    }

    private void getPromValueLine(StringBuilder builder, MetadataEntry entry, String name, Number value) {
        String metricName = getPrometheusMetricName(entry, name);
        builder.append(metricName);
        // Add tags
        String tags=entry.getTagsAsString();
        if (tags!=null && !tags.isEmpty()) {
            builder.append('{').append(tags).append('}');
        }
        Number scaledValue = MpMUnit.scaleToBase(value, entry.getUnitRaw());
        builder.append(" ").append(scaledValue).append('\n');
    }

    private void getPromTypeLine(StringBuilder builder, MetadataEntry entry, String name) {

        String metricName = getPrometheusMetricName(entry, name);
        builder.append("# TYPE ").append(metricName).append(" ").append(entry.getType()).append("\n");
    }

    private void getPromHelpLine(StringBuilder builder, MetadataEntry entry, String name) {

        String description = entry.getDescription();
        if (description !=null && !description.isEmpty()){
            String metricName = getPrometheusMetricName(entry, name);
            builder.append("# HELP ").append(metricName).append(" ").append(description).append("\n");
        }
    }

    /*
     * Create the Prometheus metric name by sanitizing some characters and then attaching the unit if
     * it is not 'none'
     */
    private String getPrometheusMetricName(MetadataEntry entry, String name) {
        String out = name.replace('-', '_').replace('.', '_');
        out = decamelize(out);
        if (!entry.getUnitRaw().equals(MpMUnit.NONE)) {
            out = out + "_" + MpMUnit.getBaseUnitAsPrometheusString(entry.getUnitRaw());
        }

        return out;
    }

    /*
     * Turn a camelCase metric name into a snake_case one like what
     * Prometheus wants.
     */
    private String decamelize(String in) {
        return in.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    /**
     * Read a value from the MBeanServer
     * @param mbeanExpression The expression to look for
     * @return The value of the Mbean attribute
     */
    private Number getValue(String mbeanExpression) {

        if (mbeanExpression==null) {
            throw new IllegalArgumentException("MBean Expression is null");
        }
        if (!mbeanExpression.contains("/")) {
            throw new NotFoundException(mbeanExpression);
        }

        int slashIndex = mbeanExpression.indexOf('/');
        String mbean = mbeanExpression.substring(0, slashIndex);
        String attName = mbeanExpression.substring(slashIndex +1);
        String subItem = null;
        if (attName.contains("#")) {
            int hashIndex = attName.indexOf('#');
            subItem = attName.substring(hashIndex +1);
            attName = attName.substring(0, hashIndex);
        }

        ObjectName objectName = null;
        try {
            objectName = new ObjectName(mbean);
            Object attribute = mbs.getAttribute(objectName,attName);
            if (attribute instanceof Number) {
                return (Number) attribute;
            }
            else if (attribute instanceof CompositeData) {
                CompositeData compositeData = (CompositeData) attribute;
                return (Number) compositeData.get(subItem);
            }
            else {
                throw new NotFoundException(mbeanExpression);
            }
        } catch (Exception e) {
            e.printStackTrace();  // TODO: Customise this generated block
            return null;
        }
    }

    private void validateSub(String sub) {
        boolean found = false;
        for (String item : bases) {
            if (sub.equals(item)) {
                found=true;
            }
        }
        if (APPLICATION.equals(sub)) {
            found=true;
        }
        if (!found) {
            throw new NotFoundException(sub);
        }
    }
}
