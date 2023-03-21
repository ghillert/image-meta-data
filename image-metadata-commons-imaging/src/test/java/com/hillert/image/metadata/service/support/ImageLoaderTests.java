/*
 * Copyright (c) 2023 Gunnar Hillert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hillert.image.metadata.service.support;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StopWatch;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

/**
 * This is a quick test to illustrate the differences between loading images using the AwtToolkit and using ImageIO.
 * For repeated invocations, AwtToolkit is dramatically faster. You will notice that the first invocation with
 * AwtToolkit is typically slower (But still faster than ImageIO). However, when invoked repeatably it is faster by
 * roughly a factor of 10.
 *
 * @author Gunnar Hillert
 */
class ImageLoaderTests {
	
	private Resource imageResource = new ClassPathResource("/test-image.jpg");

	private static List<Long> executionsAwtToolkit = new ArrayList<>();
	private static List<Long> executionsImageIO = new ArrayList<>();

	@BeforeAll
	static void beforeAll() {
		// Make sure to use headless mode ... For AwtToolkit, the first time execution without headless mode
		// will add 1000ms.
		System.setProperty("java.awt.headless", "true");
	}

	@RepeatedTest(15)
	void loadImageUsingAwtToolkit() throws MalformedURLException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ImageLoader imageLoader = new ImageLoader();
		BufferedImage image = imageLoader.loadImageUsingAwtToolkit(imageResource);
		stopWatch.stop();
		executionsAwtToolkit.add(stopWatch.getTotalTimeMillis());
		System.out.println("loadImageUsingAwtToolkit: " + stopWatch.getTotalTimeMillis());
		Assertions.assertThat(image).isNotNull();
	}

	@RepeatedTest(15)
	void loadImageUsingUsingImageIO() throws MalformedURLException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ImageLoader imageLoader = new ImageLoader();
		BufferedImage image = imageLoader.loadImageUsingImageIO(imageResource);
		stopWatch.stop();
		executionsImageIO.add(stopWatch.getTotalTimeMillis());
		System.out.println("loadImageUsingUsingImageIO: " + stopWatch.getTotalTimeMillis());
		Assertions.assertThat(image).isNotNull();
	}

	@AfterAll
	static void afterAll() {

		final LongSummaryStatistics imageIOStats = executionsImageIO
				.stream()
				.mapToLong(Long::longValue)
				.summaryStatistics();

		System.out.println(String.format(
				"imageIOStats - Executions=%s; Total time=%s ms; Average time=%s ms",
				imageIOStats.getCount(), imageIOStats.getSum(), imageIOStats.getAverage()));

		final LongSummaryStatistics awtToolkitStats = executionsAwtToolkit
				.stream()
				.mapToLong(Long::longValue)
				.summaryStatistics();

		System.out.println(String.format(
				"awtToolkitStats - Executions=%s; Total time=%s ms; Average time=%s ms",
				awtToolkitStats.getCount(), awtToolkitStats.getSum(), awtToolkitStats.getAverage()));
	}
}
