/**
 * Copyright 2013 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.apereo.lap.services;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apereo.lap.model.Output;
import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.services.output.OutputHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@TransactionConfiguration(defaultRollback = true)
@ContextConfiguration({ "classpath:test-context.xml" })
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
public class OutputHandlerServiceTest {
	private static final Logger logger = LoggerFactory
			.getLogger(OutputHandlerServiceTest.class);

	@Autowired
	ConfigurationService configuration;

	@Autowired
	OutputHandlerService outputHandler;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testDoOutput() {
		assertNotNull(outputHandler);
		assertNotNull(configuration);
		/* Testing using pipeline config */
		PipelineConfig pipelineConfig = configuration
				.getPipelineConfig("sample");
		assertNotNull(pipelineConfig);
		logger.info("Test Successful in loading 'sample' pipeline using configuration object");

		List<Output> outputs = pipelineConfig.getOutputs();
		for(Output output:outputs){
			OutputHandler.OutputResult result = outputHandler.doOutput(output);
			logger.info("Output of type:"+output.type+" complete: " + result);	
		}

	}
}
