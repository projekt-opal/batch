package org.dice_research.opal.batch;

import org.dice_research.opal.batch.misc.CreateExampleFilesTest;
import org.dice_research.opal.batch.misc.QuadsIoTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

		CountDistributionsTest.class,

		JsonExtractorTest.class,

		ReadWriteTest.class,

		// The following tests should be skipped by default

		CreateExampleFilesTest.class,

		QuadsIoTest.class

})
public class AllTests {
}