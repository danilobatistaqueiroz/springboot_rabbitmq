package br.com.labs.rabbit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RAMNodeTest {
    
    /************************************************************************************************
     * 
     * RAM nodes store internal database tables in RAM only. This does not include messages, message store indices, queue indices and other node state.
     * 
     * RAM nodes are a special case that can be used to improve the performance clusters with high queue, exchange, or binding churn.
     * 
     * RAM nodes do not provide higher message rates. 
     * 
     * Since RAM nodes store internal database tables in RAM only, they must sync them from a peer node on startup. 
     * This means that a cluster must contain at least one disk node. 
     * It is therefore not possible to manually remove the last remaining disk node in a cluster.
     * 
     * rabbitmqctl join_cluster --ram rabbit@rabbit1
     * 
     * disk nodes - store data on disk and in RAM.
     */

    @Test
    void test() {
        fail("Not yet implemented");
    }

}
