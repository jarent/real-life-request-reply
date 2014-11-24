package io.github.jarent.mule;

import java.util.List;

import org.junit.Test;
import org.mule.munit.runner.functional.FunctionalMunitSuite;

import com.google.common.collect.Lists;

public class RequestReplyTest extends FunctionalMunitSuite {
	
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
		
		 runFlow("initial-process-flow" , testEvent("1,2,3,4,5,6,7,8,9,10".split(",")));	
		
	}
	
	@Test
	public void shouldInovokeLongRunningProcessConcurrently() throws Exception {
		
		 runFlow("request-reply-flow" , testEvent("1,2,3,4,5,6,7,8,9,10".split(",")));	
		
	}
	
	@Test
	public void shouldFinishFastForLargeInputData() throws Exception {
		
		 runFlow("request-reply-flow" , testEvent(
				("1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10," +
		 		"1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10," +
		 		"1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10").split(",")));	
		
	}

}
