/* no package name */

import org.junit.*;
import static org.junit.Assert.*;

public class TestDummyC {
	@Test(timeout = 4000)
	public void test() throws Throwable {
		fail();
	}
	@Test(timeout = 4000)
	public void test2() throws Throwable {
		return;
	}
}