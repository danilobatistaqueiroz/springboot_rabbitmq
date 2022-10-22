package br.com.labs.rabbit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**************************************************************************************************************************************
 * 
 * Super streams are a way to scale out by partitioning a large stream into smaller streams.
 * They integrate with single active consumer to preserve message order within a partition.
 * 
 *  super streams particiona uma stream em streams menores
 *  serve para escalar uma stream muito grande
 *  usa single active consumer para manter a ordem das mensagens numa partição
 *  
 *  A super stream remains a logical entity: applications see it as one “big” stream, thanks to the smartness of client libraries. 
 *
 **************************************************************************************************************************************/
class SuperStreamTest {

	@Test
	void test() {
		System.out.println("Not yet implemented");
	}

}
