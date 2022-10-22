package br.com.labs.rabbit;

class ParticionamentoTest {

	void pause_if_all_down() {
	    /*********************************************************************************
	    ## configurar o arquivo rabbitmq.conf
	    ## pause_if_all_down strategy require additional configuration
	    cluster_partition_handling = pause_if_all_down

	    ## Recover strategy. Can be either 'autoheal' or 'ignore'
	    ## autoheal o próprio cluster reintegra os brokers, pode haver muitas inconsistências de dados
	    cluster_partition_handling.pause_if_all_down.recover = ignore

	    ## Node names to check
	    ## se os brokers listados não cairem, ou não ficarem fora da rede, serão considerados válidos (os dados neles serão considerados)
	    ## o lado com esses nós permanecerá, e outro lado será reiniciado e os dados perdidos  
	    cluster_partition_handling.pause_if_all_down.nodes.1 = rabbit@rabbitmq2
	    cluster_partition_handling.pause_if_all_down.nodes.2 = rabbit@rabbitmq3
	    *********************************************************************************/
	}
	
	void pause_minority() {
	    /*********************************************************************************
	    ## Pauses all nodes on the minority side of a partition. The cluster
	    ## MUST have an odd number of nodes (3, 5, etc)
	    ## num particionamento, o lado que for maioria permanece, e o que for minoria será reiniciado, os dados desconsiderados
	    cluster_partition_handling = pause_minority
	    *********************************************************************************/
	}
	
	void autoheal() {
	    /**************
	     * In autoheal mode RabbitMQ will automatically decide on a winning partition if a partition is deemed to have occurred, 
	     * and will restart all nodes that are not in the winning partition. 
	     * Unlike pause_minority mode it therefore takes effect when a partition ends, rather than when one starts.
         * The winning partition is the one which has the most clients connected (or if this produces a draw, 
         * the one with the most nodes; 
         * and if that still produces a draw then one of the partitions is chosen in an unspecified way).
         * 
         * em autoheal o Rabbitmq decide qual partição é a válida tomando a que tiver mais clientes conectados,
         * se der empate, então a que tiver mais brokers, e se ainda der empate, então é decidido aleatoriamente.
         * 
         * ao decidir a partição vencedora, na outra os servidores serão reiniciados e dos dados após o particionamento serão perdidos.
	     */
	}
	
	void ignore() {
	    /*************
	     * com ignore ao voltar do particionamento, o Rabbitmq não faz nada, então você terá que escolher qual partição considerar válida,
	     * ou usar um shovel e copiar as mensagens para um servidor paralelo, podendo copiar de uma partição que considerar melhor,
	     * ou copiar das duas partições, porém, assim provavelmente terá mensagens duplicadas
	     * docker exec server2 rabbitmqctl list_queues name, messages_ready, messages_unacknowledged, consumers, consumer_utilisation, state
	     * com esse comando é possível avaliar as mensagens num broker, já no management UI aparece o total das mensagens nas duas partições,
	     * ou seja, se numa partição a queue X tem 1 mensagem e na outra partição a mesma queue X tem 2 mensagens, no management UI vai
	     * aparecer 3 mensagens nessa queue, enquanto que com o comanto list_queues irá parecer as mensagens apenas de cada partição
	     * 
	     *  uma opção num caso de particionamento, é ir removendo todos os brokers particionados de cada lado, deixando dois clusters separados
	     *  docker exec server2 rabbitmqctl forget_cluster_node rabbit@server6
	     *  nesse exemplo, é removido o server6 do cluster
         *  Removing node rabbit@server6 from the cluster
         *  depois usar um shovel para transferir as mensagens para um dos lados e após isso resetar todos os brokers de um cluster e 
         *  adicioná-los novamente no outro cluster
	     */
	}

}
