package org.drools.benchmarks.reliability;

import org.drools.reliability.core.ReliableKieSession;
import org.drools.reliability.core.ReliableRuntimeComponentFactoryImpl;
import org.drools.reliability.core.StorageManagerFactory;
import org.drools.reliability.core.TestableStorageManager;
import org.drools.reliability.infinispan.InfinispanStorageManager;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.PersistedSessionOption;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

public abstract class AbstractReliabilityBenchmarkFailoverSupport extends AbstractReliabilityBenchmark {

    protected Long persistedSessionId;

    protected PersistedSessionOption.PersistenceStrategy persistenceStrategy; // used strategy throughout the benchmark
    protected PersistedSessionOption.SafepointStrategy safepointStrategy; // used strategy throughout the benchmark
    protected PersistedSessionOption.PersistenceObjectsStrategy persistenceObjectsStrategy; // used strategy throughout the benchmark

    @Setup(Level.Iteration)
    @Override
    public void setup() {
        // overriding setup() because failover scenario requires more complicated iteration setup
    }

    protected abstract void setupKieBase();

    protected abstract void populateKieSessionPerIteration();

    protected KieSession createKieSession(PersistedSessionOption persistedSessionOption) {
        KieSessionConfiguration conf = KieServices.get().newKieSessionConfiguration();
        if (persistedSessionOption != null) {
            conf.setOption(persistedSessionOption);
        }
        KieSession session =  kieBase.newKieSession(conf, null);
        this.persistedSessionId = persistedSessionOption == null || persistedSessionOption.isNewSession() ? session.getIdentifier() : persistedSessionOption.getSessionId();
        return session;
    }

    public void failover() {
        // EXPLICIT is not used for now, because if useSafepoints then strategy is always SafepointStrategy.AFTER_FIRE
        if (safepointStrategy == PersistedSessionOption.SafepointStrategy.EXPLICIT) {
            ((ReliableKieSession)kieSession).safepoint();
        }

        if (((TestableStorageManager) StorageManagerFactory.get().getStorageManager()).isRemote()) {
            // fail-over means restarting Drools instance. Assuming remote infinispan keeps alive
            StorageManagerFactory.get().getStorageManager().close(); // close remoteCacheManager
            // Reclaim RemoteCacheManager
            InfinispanStorageManager cacheManager = (InfinispanStorageManager) StorageManagerFactory.get().getStorageManager();
            RemoteCacheManager remoteCacheManager = container.getRemoteCacheManager(cacheManager.provideAdditionalRemoteConfigurationBuilder());
            cacheManager.setRemoteCacheManager(remoteCacheManager);
        } else {
            ((TestableStorageManager) StorageManagerFactory.get().getStorageManager()).restart(); // restart embedded infinispan cacheManager. GlobalState and FireStore are kept
        }
        ReliableRuntimeComponentFactoryImpl.refreshCounterUsingStorage();
    }

    protected KieSession restoreSession() {
        return createKieSession(PersistedSessionOption.fromSession(persistedSessionId)
                                        .withPersistenceStrategy(persistenceStrategy)
                                        .withSafepointStrategy(safepointStrategy)
                                        .withPersistenceObjectsStrategy(persistenceObjectsStrategy));
    }
}
