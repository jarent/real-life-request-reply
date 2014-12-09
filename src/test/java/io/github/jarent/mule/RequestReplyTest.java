package io.github.jarent.mule;

import java.util.List;

import org.junit.Test;
import org.mule.munit.runner.functional.FunctionalMunitSuite;

import com.google.common.collect.Lists;

public class RequestReplyTest extends FunctionalMunitSuite {
	
	private final static String SAMPLE_INPUT_10_RECORDS = "1,2,3,4,5,6,7,8,9,10";
	
	@Override
	protected String getConfigResources() {
			return   "initial-process.xml, long-running-service.xml, request-reply.xml";
	}
	
	@Override
	protected List<String> getFlowsExcludedOfInboundDisabling() {
		return Lists.newArrayList("LongRunningTask", "request-reply-long-task-split", "request-reply-long-task-aggr");
	}
	
	@Override
	protected boolean haveToMockMuleConnectors() {
		return false;
	}
	
	
	@Test
	public void shouldInovokeLongRunningProcessSequentially() throws Exception {
		
		 runFlow("initial-process-flow" , testEvent(SAMPLE_INPUT_10_RECORDS.split(",")));	
		
	}
	
	@Test
	public void shouldInovokeLongRunningProcessConcurrently() throws Exception {
		
		 runFlow("request-reply-flow" , testEvent(SAMPLE_INPUT_10_RECORDS.split(",")));	
		
	}
	
	@Test
	public void shouldFinishFastForLargeInputData() throws Exception {
		
		 StringBuilder sampleInput1000Records = new StringBuilder();
		 for (int i = 0; i <= 100; ++i) {
			 sampleInput1000Records.append(SAMPLE_INPUT_10_RECORDS);
			 if (i != 100) {
				 sampleInput1000Records.append(",");
			 }
		 }
		
		 runFlow("request-reply-flow" , testEvent(
				 sampleInput1000Records.toString().split(",")));	
		
	}

}
