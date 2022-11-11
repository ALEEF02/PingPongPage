package ppp.service;

import java.util.HashMap;
import java.util.Map;

import ppp.db.controllers.CServiceVariables;
import ppp.db.controllers.CServices;
import ppp.db.model.OServiceVariable;

public abstract class AbstractService {
    private Map<String, OServiceVariable> cache;
    private long cachedLastRun = 0L;

    public AbstractService() {
        cache = new HashMap<>();
    }

    /**
     * Start the service
     */
    public final void start() {
        if (cachedLastRun == 0L) {
            cachedLastRun = Long.parseLong("0" + getData("abs_last_service_run"));
        }
        long now = System.currentTimeMillis();
        long next = cachedLastRun + getDelayBetweenRuns();
        if (next <= now) {
            if (!shouldIRun()) {
                return;
            }
            beforeRun();
            System.out.println("Running " + getIdentifier());
            try {
                run();
            } catch (Exception e) {
                System.out.println(e + ", service, " + getIdentifier());
            }
            afterRun();
            saveData("abs_last_service_run", now);
            cachedLastRun = now;
        }
    }

    /**
     * Gets data for a certain key and caches it
     *
     * @param key The key needed
     * @return the value of the key
     */
    protected String getData(String key) {
        return getDataObject(key).value;
    }

    /**
     * Gets data for a certain key and caches it
     *
     * @param key The key used
     * @return the database row object for
     */
    private OServiceVariable getDataObject(String key) {
        if (!cache.containsKey(key)) {
            cache.put(key, CServiceVariables.findBy(getIdentifier(), key));
        }
        return cache.get(key);
    }


    /**
     * Saves service data
     *
     * @param key   the key
     * @param value Any value converted to string
     */
    protected void saveData(String key, Object value) {
        OServiceVariable dataObject = getDataObject(key);
        dataObject.variable = key;
        dataObject.serviceId = CServices.getCachedId(getIdentifier());
        dataObject.value = String.valueOf(value);
        CServiceVariables.insertOrUpdate(dataObject);
    }

    /**
     * The identifier of the service. This is used to reference the service and the key to store data with.
     *
     * @return the identifier of the service
     */
    public abstract String getIdentifier();

    /**
     * Milliseconds the service should wait between each run
     *
     * @return delay in milliseconds
     */
    public abstract long getDelayBetweenRuns();

    /**
     * Determines if the service should run
     *
     * @return should it run?
     */
    public abstract boolean shouldIRun();

    /**
     * Called before {@link #run() run()}, so things can be prepared if needed
     */
    public abstract void beforeRun();

    /**
     * The actual handling of the service
     */
    public abstract void run() throws Exception;

    /**
     * Called after {@link #run() run()}, can be used to clean up things if needed
     */
    public abstract void afterRun();
}
