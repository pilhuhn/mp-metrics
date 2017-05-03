package de.bsd.mp_metrics;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
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

    private static final String[] bases = {"base","vendor"};
    private static final String APPLICATION = "application";
    MBeanServer mbs;

    @Inject
    ApplicationMetric applicationMetric;

    public MpMetricsWorker() {
        mbs = ManagementFactory.getPlatformMBeanServer();
    }

    @GET
    public Map<String, Map<String,Number>> getAllValues() {
        Map<String, Map<String,Number>> results = new HashMap<>(bases.length);
        for (String subTree : bases) {
            results.put(subTree, getValuesForSubTree(subTree));
        }
        results.put("application", getAppValues());
        return results;
    }

    @GET
    @Path("/{sub}")
    public Map<String, Number> getBaseValues(@PathParam("sub")String sub) {

        validateSub(sub);

        if (sub.equals(APPLICATION)) {
            return getAppValues();
        }
        return getValuesForSubTree(sub);
    }

    private Map<String, Number> getAppValues() {
        return applicationMetric.getAll();
    }


    @GET
    @Path("/{sub}/{field}")
    public Map<String,Number> getValueForField(@PathParam("sub") String sub, @PathParam("field") String field) {

        List<MetadataEntry> metadata = ConfigHolder.getInstance().getConfig().get(sub);
        Map<String,Number> results = new HashMap<>(1);

        for (MetadataEntry entry : metadata) {
            if (entry.getName().equals(field)) {

                Number value = getValue(entry.getMbean());
                results.put(entry.getName(), value);

                return results;
            }
        }
        throw new NotFoundException(sub + " / "+ field);
    }

    @OPTIONS
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
        return new ArrayList<>(applicationMetric.getAllMetaData().values());
    }

    @OPTIONS
    @Path("/{sub}")
    public List<MetadataEntry> getMetadataForSubTree(@PathParam("sub")String sub) {

        validateSub(sub);
        List<MetadataEntry> metadata = ConfigHolder.getInstance().getConfig().get(sub);
        return metadata;
    }

    private Map<String, Number> getValuesForSubTree(String sub) {
        List<MetadataEntry> metadata = ConfigHolder.getInstance().getConfig().get(sub);

        Map<String,Number> results = new HashMap<>(metadata.size());
        for (MetadataEntry entry : metadata) {
            Number value = getValue(entry.getMbean());
            results.put(entry.getName(),value);
        }

        return results;
    }

    /**
     * Read a value from the MBeanServer
     * @param mbeanExpression
     * @return
     */
    private Number getValue(String mbeanExpression) {


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
            if (APPLICATION.equals(item)) {
                found=true;
            }
        }
        if (!found) {
            throw new NotFoundException(sub);
        }
    }
}
