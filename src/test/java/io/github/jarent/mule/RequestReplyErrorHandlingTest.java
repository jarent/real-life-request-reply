package io.github.jarent.mule;

import static org.mule.munit.common.mocking.Attribute.attribute;

import java.util.List;

import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.modules.interceptor.processors.MuleMessageTransformer;
import org.mule.munit.runner.functional.FunctionalMunitSuite;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class RequestReplyErrorHandlingTest extends FunctionalMunitSuite {
	
	
	@Override
	protected String getConfigResources() {
			return   "long-running-service.xml, request-reply-error-handling.xml";
	}
	
	@Override
	protected List<String> getFlowsExcludedOfInboundDisabling() {
		return Lists.newArrayList("LongRunningTask", "request-reply-long-task-split-with-error-handling", "request-reply-long-task-aggr-with-error-handling");
	}
	
	@Override
	protected boolean haveToMockMuleConnectors() {
		return false;
	}
	
	@Test
	public void shouldSendMessageIfNoErrors() throws Exception {
		
		runFlow("request-reply-error-handling-flow" , testEvent("1,2,3".split(",")));		
		
		verifyCallOfMessageProcessor("outbound-endpoint").ofNamespace("vm").withAttributes(ImmutableMap.of("path", (Object)"finalMessage")).times(1);
			
	}
	
	@Test
	public void shouldStopeProcessingWhenLongRunningTaskFails() throws Exception {
		
		//emulate exception
		whenMessageProcessor("component").ofNamespace("scripting").withAttributes(attribute("name").
					ofNamespace("doc").withValue("Long running activity")).
					thenThrow(new RuntimeException());
		
		
		runFlow("request-reply-error-handling-flow" , testEvent("1".split(",")));	
		
		
		verifyCallOfMessageProcessor("logger").withAttributes(attribute("name").ofNamespace("doc").withValue("Processing Failed Logger")).times(1);
		verifyCallOfMessageProcessor("outbound-endpoint").ofNamespace("vm").withAttributes(attribute("path").withValue("finalMessage")).times(0);
	}
	
	@Test(expected=org.mule.api.routing.ResponseTimeoutException.class)
	public void shouldStopeProcessingWhenLongRunningTaskIsStuck() throws Exception {
		
		

		
		//emulate long running task stuck - request/reply timeout should occur
		whenMessageProcessor("component").ofNamespace("scripting").withAttributes(attribute("name").
					ofNamespace("doc").withValue("Long running activity")).
					thenApply(
							new MuleMessageTransformer() {

								@Override
								public MuleMessage transform(MuleMessage arg0) {
									//15s - more than 11s request-reply timeout
									try {
										Thread.sleep(15000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									return null;
								}
						
					});
		
		
		runFlow("request-reply-error-handling-flow" , testEvent("1".split(",")));	
		
	}
	
}