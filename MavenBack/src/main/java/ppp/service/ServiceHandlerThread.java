package ppp.service;

import ppp.service.AbstractService;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServiceHandlerThread extends Thread {
	private List<AbstractService> instances;
	
	public ServiceHandlerThread() {
		super("ServiceHandler");
	    instances = new ArrayList<>();
	}
	
	// Load all of the services under ppp.service.services into the thread's cache
	private void initServices() {
		Reflections reflections = new Reflections("ppp.service.services");
		Set<Class<? extends AbstractService>> classes = reflections.getSubTypesOf(AbstractService.class);
		for (Class<? extends AbstractService> serviceClass : classes) {
			try {
				instances.add(serviceClass.getConstructor().newInstance());
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

    @Override
    public void run() {
        boolean initialized = false;
        while (true) {
            try {
                if (!initialized) {
                	//System.out.print("Initializating services...");
                    initServices();
                    initialized = true;
                	//System.out.println(" Done.");
                }
                for (AbstractService instance : instances) {
                    instance.start();
                }
                sleep(600_000L); // Run every 10 minutes
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
