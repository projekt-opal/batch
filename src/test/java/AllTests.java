import org.dice_research.opal.batch.ReadWriteTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import misc.CreateExampleFilesTest;
import misc.QuadsIoTest;

@RunWith(Suite.class)
@SuiteClasses({

		ReadWriteTest.class,

		// The following tests should be skipped by default

		CreateExampleFilesTest.class,

		QuadsIoTest.class

})
public class AllTests {
}