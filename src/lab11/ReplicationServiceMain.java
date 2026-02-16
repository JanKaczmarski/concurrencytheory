package lab11;

import java.util.Arrays;
import java.util.List;

// ** PORTS **
interface Replicator {
    void startReplication(String tenantId);
    void stopReplication(String tenantId);
}

// ** ADAPTERS **
class S3ReplicatorAdapter implements Replicator {
    @Override
    public void startReplication(String tenantId) {
        // Start S3 replication
    }

    @Override
    public void stopReplication(String tenantId) {
        // Stop S3 replication
    }
}
class AuroraReplicatorAdapter implements Replicator {
    @Override
    public void startReplication(String tenantId) {
        // Start Aurora replication
    }

    @Override
    public void stopReplication(String tenantId) {
        // Stop Aurora replication
    }
}

// ** DOMAIN **
public class ReplicationServiceMain {
    public static void main(String[] args) {
        String tenantId = "tenant-123";

        // In a real app, these are injected via Dependency Injection
        List<Replicator> activePlugins = Arrays.asList(
                new S3ReplicatorAdapter(),
                new AuroraReplicatorAdapter()
        );

        for (Replicator plugin : activePlugins) {
            plugin.startReplication(tenantId);
            System.out.println("---");
        }

        // .....
    }
}
