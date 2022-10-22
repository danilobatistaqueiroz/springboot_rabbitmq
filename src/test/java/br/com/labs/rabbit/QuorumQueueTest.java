package br.com.labs.rabbit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

class QuorumQueueTest {

    /**********************************************************************************************************************************************************
    * Quorum queues do not support priorities. Instead, use multiple queues and assign different queues to process different priorities; one for each priority.
    *
    * Não é possível aplicar dead-letter em quorum queues
    * 
    * O número de réplicas default é de 5, se um cluster tem 9 nós, apenas 5 terão as queues do quorum
    * 
    * No caso de particionamento, um novo leader é eleito no lado com maioria. No lado de minoria não terá mais progresso (não aceitará mais mensagens, não entregará mais..)
    *  
    * Quorum queues have the same ordering guarantees as any queue but are also able to deliver messages from a local replica. 
    * How they achieve this is interesting but not relevant to developers or administrators. 
    * What IS useful is understanding that this is another reason to choose quorum queues over mirrored queues. 
    * We previously described the very network inefficient algorithm behind mirrored queues, and now you’ve seen that with quorum queues 
    * we have heavily optimised network utilisation.
    * 
    * Quorum vs Classic Mirrored:
    *   Quorum queues seek to address all the limitations of classic mirrored queues and add more benefits. 
    *   Quorum queues are safer and achieve significantly higher throughput than classic mirrored queues and Quorum queues run through the Raft protocol, 
    *   the widely known consensus algorithm.
    *   Raft protocol usa heartbeat, o leader envia de tempo em tempo um sinal de vida para os followers, se houver um particionamento, os followers 
    *   da parte do particionamento que é minoria vão tentar eleger um novo leader, porém, para isso é necessário um mínimo de quorum de followers,
    *   e por ser o particionamento menor, não terá esse quorum, apenas o particionamento maior, isso evita o split-brain. 
    * 
    *   There is no ‘stop the world’ synchronization or availability vs. consistency choice to make. 
    *   Quorum queues will only approve and confirm a command when it is replicated to a quorum of its nodes. 
    *   Availability is only lost when the majority of the nodes are down.
    *   
    *   Quorum queues are also simplistic compared to classic queues regarding network partition. 
    *   That’s because they use a separate, quick failure detector. 
    *   This results in a quick restoration of availability or no impact at all.
    **********************************************************************************************************************************************************/
    
	@Test
	void test() throws IOException {
	    Channel ch = null; //= connection.createChannel();
	    
        String queue = "qquorum1";
        String exchange = "exchange3";

        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-queue-type", "quorum");
        args.put("x-expires", 15000);
        args.put("x-delivery-limit", 5);

        ch.queueDeclare(queue, true, false, false, args);
        ch.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, false, true, null);
        ch.queueBind(queue, exchange, "tests");
	}

}
